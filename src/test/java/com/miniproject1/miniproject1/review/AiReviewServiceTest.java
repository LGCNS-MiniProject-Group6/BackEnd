package com.miniproject1.miniproject1.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.review.AiReviewAnalysis.Condition;
import com.miniproject1.miniproject1.review.AiReviewAnalysis.GroundedText;
import com.miniproject1.miniproject1.review.common.client.AiModelClient;
import com.miniproject1.miniproject1.review.common.context.AiProgramContext;
import com.miniproject1.miniproject1.review.common.context.AiProgramContextRepository;
import com.miniproject1.miniproject1.review.common.context.AiProgramSummary;
import com.miniproject1.miniproject1.review.common.prompt.AiPrompt;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiReviewServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PBLANC_ID = "PBLN-1";
    private static final String ORIGINAL_TEXT = "서울 소재 소상공인을 대상으로 합니다. 사업자등록증을 제출하세요.";

    @Mock
    private AiModelClient modelClient;

    @Mock
    private AiProgramContextRepository contextRepository;

    @Mock
    private AiReviewRepository reviewRepository;

    private AiReviewService reviewService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        reviewService = new AiReviewService(
                modelClient,
                contextRepository,
                reviewRepository,
                objectMapper);
    }

    @Test
    void 이메일과_공고ID만으로_검수하고_저장한다() {
        AiReviewAnalysis analysis = matchedAnalysis();
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(analysis);
        when(reviewRepository.save(
                eq(EMAIL),
                eq(PBLANC_ID),
                any(AiBusinessProfile.class),
                eq(AiReviewStatus.MATCHED),
                eq(analysis)))
                .thenReturn(15L);

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.reviewId()).isEqualTo(15L);
        assertThat(result.status()).isEqualTo(AiReviewStatus.MATCHED);

        ArgumentCaptor<AiPrompt> promptCaptor = ArgumentCaptor.forClass(AiPrompt.class);
        verify(modelClient).generateStructured(promptCaptor.capture(), eq(AiReviewAnalysis.class));
        assertThat(promptCaptor.getValue().userMessage())
                .contains("\"region\":\"서울\"")
                .doesNotContain(EMAIL);
    }

    @Test
    void 불충족_조건이_있으면_전체_상태는_UNMATCHED이다() {
        AiReviewAnalysis analysis = new AiReviewAnalysis(
                List.of(
                        matchedCondition(),
                        new Condition(
                                "지역",
                                AiReviewStatus.UNMATCHED,
                                "서울",
                                "부산 소재 사업자",
                                "지역 요건이 일치하지 않습니다.",
                                "서울 소재 소상공인을 대상으로 합니다.")),
                List.of(),
                List.of());
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(analysis);
        when(reviewRepository.save(
                eq(EMAIL),
                eq(PBLANC_ID),
                any(AiBusinessProfile.class),
                eq(AiReviewStatus.UNMATCHED),
                eq(analysis)))
                .thenReturn(16L);

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.status()).isEqualTo(AiReviewStatus.UNMATCHED);
    }

    @Test
    void 사업정보가_없으면_AI를_호출하지_않는다() {
        when(contextRepository.findByPblancId(PBLANC_ID)).thenReturn(Optional.of(programContext()));
        when(contextRepository.findOriginalTextByPblancId(PBLANC_ID)).thenReturn(Optional.of(ORIGINAL_TEXT));
        when(reviewRepository.findBusinessProfile(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.review(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BUSINESS_INFO_NOT_FOUND);
        verify(modelClient, never()).generateStructured(any(), any());
    }

    @Test
    void 원문에_없는_근거는_저장하지_않는다() {
        AiReviewAnalysis invalidAnalysis = new AiReviewAnalysis(
                List.of(new Condition(
                        "지역",
                        AiReviewStatus.MATCHED,
                        "서울",
                        "서울 소재 사업자",
                        "지역 요건을 충족합니다.",
                        "원문에 존재하지 않는 문장")),
                List.of(),
                List.of());
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(invalidAnalysis);

        assertThatThrownBy(() -> reviewService.review(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
        verify(reviewRepository, never()).save(any(), any(), any(), any(), any());
    }

    private void prepareInputs() {
        when(contextRepository.findByPblancId(PBLANC_ID)).thenReturn(Optional.of(programContext()));
        when(contextRepository.findOriginalTextByPblancId(PBLANC_ID)).thenReturn(Optional.of(ORIGINAL_TEXT));
        when(reviewRepository.findBusinessProfile(EMAIL)).thenReturn(Optional.of(businessProfile()));
    }

    private AiReviewAnalysis matchedAnalysis() {
        return new AiReviewAnalysis(
                List.of(matchedCondition()),
                List.of(),
                List.of(new GroundedText(
                        "사업자등록증",
                        "사업자등록증을 제출하세요.")));
    }

    private Condition matchedCondition() {
        return new Condition(
                "지역",
                AiReviewStatus.MATCHED,
                "서울",
                "서울 소재 사업자",
                "지역 요건을 충족합니다.",
                "서울 소재 소상공인을 대상으로 합니다.");
    }

    private AiBusinessProfile businessProfile() {
        return new AiBusinessProfile(
                "서울",
                "카페",
                LocalDate.of(2024, 3, 15),
                "개인사업자",
                0,
                80_000_000L,
                List.of("마케팅"));
    }

    private AiProgramContext programContext() {
        return new AiProgramContext(
                PBLANC_ID,
                "온라인 판로 지원",
                "판로",
                "중소벤처기업부",
                "서울 소재 소상공인",
                "온라인 판매 지원",
                "2026-09-01~2026-09-30",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                new AiProgramSummary(
                        "온라인 판로 확대 사업",
                        "서울 소재 소상공인",
                        "온라인 입점 지원",
                        "온라인 신청",
                        "사업자등록증",
                        "중소벤처기업부"));
    }
}
