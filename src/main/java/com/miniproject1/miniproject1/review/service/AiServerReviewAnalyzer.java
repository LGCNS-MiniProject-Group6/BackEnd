package com.miniproject1.miniproject1.review.service;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.review.dto.ai.AiReviewRequest;
import com.miniproject1.miniproject1.review.dto.ai.AiReviewResponse;
import com.miniproject1.miniproject1.review.entity.ReviewConditionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
public class AiServerReviewAnalyzer implements ReviewAnalyzer {

    private final RestClient restClient;
    private final String internalServiceSecret;

    public AiServerReviewAnalyzer(
            RestClient.Builder restClientBuilder,
            @Value("${ai.server.url:http://localhost:8001}") String aiServerUrl,
            @Value("${ai.server.secret:}") String internalServiceSecret) {
        this.restClient = restClientBuilder.baseUrl(aiServerUrl).build();
        this.internalServiceSecret = internalServiceSecret;
    }

    @Override
    public ReviewAnalysis analyze(Program program, BusinessInfo businessInfo) {
        AiReviewResponse response;
        try {
            RestClient.RequestBodySpec request = restClient.post()
                    .uri("/api/reviews/analyze");
            request.body(AiReviewRequest.from(program.getPblancId(), businessInfo));

            if (internalServiceSecret != null && !internalServiceSecret.isBlank()) {
                request.header("X-Internal-Service-Key", internalServiceSecret);
            }

            response = request.retrieve()
                    .onStatus(HttpStatusCode::isError, (requestInfo, responseInfo) -> {
                        throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                                "AI 서버가 검수 요청을 처리하지 못했습니다.");
                    })
                    .body(AiReviewResponse.class);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.error("AI 서버 연결 실패", exception);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "AI 서버에 연결하지 못했습니다.");
        }

        if (response == null || response.overallStatus() == null || response.conditions() == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "AI 서버 응답 형식이 올바르지 않습니다.");
        }

        validateStatus(response.overallStatus(), List.of("ELIGIBLE", "INELIGIBLE", "NEED_CHECK"));
        List<ReviewAnalyzer.ConditionAnalysis> conditions = response.conditions().stream()
                .map(condition -> {
                    if (condition.conditionType() == null || condition.status() == null) {
                        throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                                "AI 서버 조건 응답이 올바르지 않습니다.");
                    }
                    validateStatus(condition.status(), List.of("MATCHED", "UNMATCHED", "NEED_CHECK"));
                    return new ReviewAnalyzer.ConditionAnalysis(
                            condition.conditionType(),
                            ReviewConditionStatus.valueOf(condition.status()),
                            condition.reason(), condition.page(), condition.evidenceText());
                })
                .toList();

        return new ReviewAnalysis(
                response.overallStatus(), response.model(), response.promptVersion(),
                response.documentHash(), conditions);
    }

    private void validateStatus(String value, List<String> allowed) {
        if (!allowed.contains(value)) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                    "AI 서버 응답에 허용되지 않은 상태값이 포함되어 있습니다.");
        }
    }
}
