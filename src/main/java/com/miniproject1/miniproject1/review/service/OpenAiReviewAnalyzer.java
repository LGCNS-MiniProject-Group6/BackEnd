package com.miniproject1.miniproject1.review.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.review.entity.ReviewConditionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenAiReviewAnalyzer implements ReviewAnalyzer {

    private static final String PROMPT_VERSION = "review-v1";
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiReviewAnalyzer(ChatClient.Builder chatClientBuilder,
                                ObjectMapper objectMapper,
                                @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public ReviewAnalysis analyze(Program program, BusinessInfo businessInfo) {
        String prompt = """
                다음 사업자 정보와 지원사업 공고를 비교해 신청 가능성을 검수하세요.
                공고에 명시된 내용만 근거로 사용하고, 근거가 없으면 NEED_CHECK로 표시하세요.
                개인정보(이름, 이메일, 전화번호)는 입력으로 전달하지 않습니다.

                사업자 정보:
                업종=%s, 지역=%s, 개업일=%s, 직원수=%s, 연매출=%s

                공고 정보:
                공고ID=%s, 제목=%s, 분야=%s, 대상=%s, 내용=%s

                반드시 아래 JSON만 반환하세요.
                {"overallStatus":"ELIGIBLE|INELIGIBLE|NEED_CHECK", "conditions":[
                {"conditionType":"REGION|INDUSTRY|OPENING_DATE|EMPLOYEE_COUNT|ANNUAL_REVENUE|OTHER",
                "status":"MATCHED|UNMATCHED|NEED_CHECK", "reason":"판단 이유", "page":null,
                "evidenceText":"공고문 근거"}]}
                """.formatted(
                businessInfo.getIndustry(), businessInfo.getRegion(), businessInfo.getOpeningDate(),
                businessInfo.getEmployeeCount(), businessInfo.getAnnualRevenue(), program.getPblancId(),
                program.getTitle(), program.getCategory(), program.getTargetDescription(), program.getDescription());

        try {
            String content = chatClient.prompt().user(prompt).call().content();
            JsonNode root = objectMapper.readTree(stripMarkdownFence(content));
            String overallStatus = root.path("overallStatus").asText(null);
            if (!List.of("ELIGIBLE", "INELIGIBLE", "NEED_CHECK").contains(overallStatus)) {
                throw new IllegalArgumentException("유효하지 않은 overallStatus");
            }

            List<ConditionAnalysis> conditions = new ArrayList<>();
            JsonNode conditionNodes = root.path("conditions");
            if (!conditionNodes.isArray()) {
                throw new IllegalArgumentException("conditions가 배열이 아닙니다.");
            }
            for (JsonNode node : conditionNodes) {
                String type = node.path("conditionType").asText(null);
                String status = node.path("status").asText(null);
                if (type == null || !List.of("MATCHED", "UNMATCHED", "NEED_CHECK").contains(status)) {
                    throw new IllegalArgumentException("조건 필드가 유효하지 않습니다.");
                }
                conditions.add(new ConditionAnalysis(
                        type,
                        ReviewConditionStatus.valueOf(status),
                        node.path("reason").asText(""),
                        node.path("page").isNumber() ? node.path("page").asInt() : null,
                        node.path("evidenceText").asText("")));
            }
            return new ReviewAnalysis(overallStatus, model, PROMPT_VERSION, null, conditions);
        } catch (Exception exception) {
            log.error("AI 검수 응답 처리 실패", exception);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "AI 검수 결과를 처리하지 못했습니다.");
        }
    }

    private String stripMarkdownFence(String content) {
        if (content == null) {
            throw new IllegalArgumentException("AI 응답이 비어 있습니다.");
        }
        return content.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
    }
}
