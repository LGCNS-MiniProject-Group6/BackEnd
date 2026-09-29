package com.miniproject1.miniproject1.aireview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.aireview.AiReviewAnalysis.Condition;
import com.miniproject1.miniproject1.aireview.AiReviewAnalysis.GroundedText;
import com.miniproject1.miniproject1.aireview.common.client.AiModelClient;
import com.miniproject1.miniproject1.aireview.common.prompt.AiPrompt;
import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.entity.BusinessInterestCategory;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.business.repository.BusinessInterestCategoryRepository;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.entity.ProgramDocument;
import com.miniproject1.miniproject1.program.repository.AiSummaryRepository;
import com.miniproject1.miniproject1.program.repository.ProgramDocumentRepository;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
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
    private ProgramRepository programRepository;

    @Mock
    private AiSummaryRepository aiSummaryRepository;

    @Mock
    private ProgramDocumentRepository programDocumentRepository;

    @Mock
    private BusinessInfoRepository businessInfoRepository;

    @Mock
    private BusinessInterestCategoryRepository interestCategoryRepository;

    @Mock
    private AiReviewRepository reviewRepository;

    private AiReviewService reviewService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        reviewService = new AiReviewService(
                modelClient,
                programRepository,
                aiSummaryRepository,
                programDocumentRepository,
                businessInfoRepository,
                interestCategoryRepository,
                reviewRepository,
                objectMapper);
    }

    @Test
    void 이메일과_공고ID만으로_검수하고_저장한다() {
        AiReviewAnalysis analysis = matchedAnalysis();
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(analysis);
        AiReview saved = savedReview(15L, AiReviewStatus.MATCHED, analysis);
        when(reviewRepository.save(any(AiReview.class))).thenReturn(saved);

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.reviewId()).isEqualTo(15L);
        assertThat(result.status()).isEqualTo(AiReviewStatus.MATCHED);

        ArgumentCaptor<AiPrompt> promptCaptor = ArgumentCaptor.forClass(AiPrompt.class);
        verify(modelClient).generateStructured(promptCaptor.capture(), eq(AiReviewAnalysis.class));
        assertThat(promptCaptor.getValue().userMessage())
                .contains("\"region\":\"서울\"")
                .doesNotContain(EMAIL);

        ArgumentCaptor<AiReview> entityCaptor = ArgumentCaptor.forClass(AiReview.class);
        verify(reviewRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(entityCaptor.getValue().getPblancId()).isEqualTo(PBLANC_ID);
        assertThat(entityCaptor.getValue().getStatus()).isEqualTo(AiReviewStatus.MATCHED);
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
        when(reviewRepository.save(any(AiReview.class)))
                .thenReturn(savedReview(16L, AiReviewStatus.UNMATCHED, analysis));

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.status()).isEqualTo(AiReviewStatus.UNMATCHED);
    }

    @Test
    void 조사가_다르게_인용된_근거도_어근_기준으로_통과한다() {
        AiReviewAnalysis analysis = new AiReviewAnalysis(
                List.of(new Condition(
                        "지역",
                        AiReviewStatus.MATCHED,
                        "서울",
                        "서울 소재 사업자",
                        "지역 요건을 충족합니다.",
                        "서울 소재 소상공인을 대상으로는 사업자등록증은 필요합니다.")),
                List.of(),
                List.of());
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(analysis);
        when(reviewRepository.save(any(AiReview.class)))
                .thenReturn(savedReview(21L, AiReviewStatus.MATCHED, analysis));

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.status()).isEqualTo(AiReviewStatus.MATCHED);
    }

    @Test
    void 사업정보가_없으면_AI를_호출하지_않는다() {
        ProgramDocument document = document(ORIGINAL_TEXT);
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.of(program()));
        when(programDocumentRepository.findById(PBLANC_ID)).thenReturn(Optional.of(document));
        when(businessInfoRepository.findFirstByEmailOrderByIsDefaultDescBusinessIdAsc(EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.review(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BUSINESS_INFO_NOT_FOUND);
        verify(modelClient, never()).generateStructured(any(), any());
    }

    @Test
    void 원문에_없는_근거는_전체를_거부하지_않고_NEED_CHECK로_강등해_저장한다() {
        AiReviewAnalysis ungroundedAnalysis = new AiReviewAnalysis(
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
                .thenReturn(ungroundedAnalysis);
        when(reviewRepository.save(any(AiReview.class)))
                .thenReturn(savedReview(22L, AiReviewStatus.NEED_CHECK, ungroundedAnalysis));

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.status()).isEqualTo(AiReviewStatus.NEED_CHECK);

        ArgumentCaptor<AiReview> entityCaptor = ArgumentCaptor.forClass(AiReview.class);
        verify(reviewRepository).save(entityCaptor.capture());
        Condition savedCondition = entityCaptor.getValue().getResultDetail().conditions().get(0);
        assertThat(savedCondition.status()).isEqualTo(AiReviewStatus.NEED_CHECK);
        assertThat(savedCondition.evidence()).isNull();
    }

    @Test
    void 근거가_빈_문자열이면_NEED_CHECK로_강등해_저장한다() {
        AiReviewAnalysis blankEvidenceAnalysis = new AiReviewAnalysis(
                List.of(new Condition(
                        "업종",
                        AiReviewStatus.MATCHED,
                        "카페",
                        "중소기업",
                        "업종 요건을 확인할 수 없습니다.",
                        "")),
                List.of(),
                List.of());
        prepareInputs();
        when(modelClient.generateStructured(any(AiPrompt.class), eq(AiReviewAnalysis.class)))
                .thenReturn(blankEvidenceAnalysis);
        when(reviewRepository.save(any(AiReview.class)))
                .thenReturn(savedReview(23L, AiReviewStatus.NEED_CHECK, blankEvidenceAnalysis));

        AiReviewResult result = reviewService.review(EMAIL, PBLANC_ID);

        assertThat(result.status()).isEqualTo(AiReviewStatus.NEED_CHECK);
        verify(reviewRepository).save(any(AiReview.class));
    }

    private void prepareInputs() {
        ProgramDocument document = document(ORIGINAL_TEXT);
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.of(program()));
        when(programDocumentRepository.findById(PBLANC_ID)).thenReturn(Optional.of(document));
        when(aiSummaryRepository.findById(PBLANC_ID)).thenReturn(Optional.empty());
        when(businessInfoRepository.findFirstByEmailOrderByIsDefaultDescBusinessIdAsc(EMAIL))
                .thenReturn(Optional.of(businessInfo()));
        when(interestCategoryRepository.findByBusinessIdOrderByCategory(1L))
                .thenReturn(List.of(BusinessInterestCategory.builder()
                        .businessId(1L)
                        .category("마케팅")
                        .build()));
    }

    private AiReview savedReview(long id, AiReviewStatus status, AiReviewAnalysis analysis) {
        AiReview review = AiReview.builder()
                .email(EMAIL)
                .pblancId(PBLANC_ID)
                .businessSnapshot(businessProfile())
                .status(status)
                .resultDetail(analysis)
                .build();
        try {
            var idField = AiReview.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(review, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return review;
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

    private BusinessInfo businessInfo() {
        return BusinessInfo.builder()
                .businessId(1L)
                .email(EMAIL)
                .industry("카페")
                .region("서울")
                .openingDate(LocalDate.of(2024, 3, 15))
                .businessType("개인사업자")
                .employeeCount(0)
                .annualRevenue(80_000_000L)
                .isDefault(true)
                .build();
    }

    private Program program() {
        return Program.builder()
                .pblancId(PBLANC_ID)
                .title("온라인 판로 지원")
                .category("판로")
                .organization("중소벤처기업부")
                .targetDescription("서울 소재 소상공인")
                .description("온라인 판매 지원")
                .rawApplyPeriod("2026-09-01~2026-09-30")
                .applyStartDate(LocalDate.of(2026, 9, 1))
                .applyEndDate(LocalDate.of(2026, 9, 30))
                .build();
    }

    private ProgramDocument document(String originalText) {
        ProgramDocument document = mock(ProgramDocument.class);
        when(document.getOriginalText()).thenReturn(originalText);
        return document;
    }
}
