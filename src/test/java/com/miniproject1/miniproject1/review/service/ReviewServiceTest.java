package com.miniproject1.miniproject1.review.service;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import com.miniproject1.miniproject1.review.entity.ReviewConditionStatus;
import com.miniproject1.miniproject1.review.entity.ReviewStatus;
import com.miniproject1.miniproject1.review.entity.Review;
import com.miniproject1.miniproject1.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PBLANC_ID = "PROGRAM-01";

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BusinessInfoRepository businessInfoRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ReviewAnalyzer reviewAnalyzer;

    @InjectMocks
    private ReviewService reviewService;

    private final BusinessInfo businessInfo = BusinessInfo.builder()
            .email(EMAIL)
            .industry("제조업")
            .region("서울")
            .openingDate(LocalDate.of(2020, 1, 1))
            .employeeCount(3)
            .annualRevenue(100_000_000L)
            .build();

    private final Program program = Program.builder()
            .pblancId(PBLANC_ID)
            .title("소상공인 지원사업")
            .category("시설개선")
            .targetDescription("서울 소재 소상공인")
            .description("시설개선 비용 지원")
            .build();

    @Test
    void 검수결과를_저장한다() {
        ReviewAnalyzer.ReviewAnalysis analysis = new ReviewAnalyzer.ReviewAnalysis(
                "ELIGIBLE", "test-model", "review-v1", null,
                List.of(new ReviewAnalyzer.ConditionAnalysis(
                        "REGION", ReviewConditionStatus.MATCHED, "지역이 일치합니다.", 1, "서울 소재")));
        when(businessInfoRepository.findByEmail(EMAIL)).thenReturn(Optional.of(businessInfo));
        when(programRepository.findById(PBLANC_ID)).thenReturn(Optional.of(program));
        when(reviewAnalyzer.analyze(program, businessInfo)).thenReturn(analysis);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reviewService.create(EMAIL, PBLANC_ID);

        assertThat(response.overallStatus()).isEqualTo(ReviewStatus.ELIGIBLE);
        assertThat(response.conditions()).singleElement().satisfies(condition -> {
            assertThat(condition.conditionType()).isEqualTo("REGION");
            assertThat(condition.status()).isEqualTo(ReviewConditionStatus.MATCHED);
        });
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void 사업정보가_없으면_AI를_호출하지_않는다() {
        when(businessInfoRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(EMAIL, PBLANC_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BUSINESS_INFO_NOT_FOUND);
        verify(reviewAnalyzer, never()).analyze(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void 내_검수이력만_페이지로_조회한다() {
        Review review = Review.builder()
                .email(EMAIL)
                .pblancId(PBLANC_ID)
                .overallStatus(ReviewStatus.NEED_CHECK)
                .build();
        var page = new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1);
        when(reviewRepository.findAllByEmailOrderByCreatedAtDesc(EMAIL, PageRequest.of(0, 20)))
                .thenReturn(page);

        var response = reviewService.findAll(EMAIL, PageRequest.of(0, 20));

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).singleElement()
                .extracting(item -> item.pblancId()).isEqualTo(PBLANC_ID);
    }

    @Test
    void 다른_사용자의_검수상세는_조회할수없다() {
        when(reviewRepository.findByIdAndEmail(1L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.findById(EMAIL, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }
}
