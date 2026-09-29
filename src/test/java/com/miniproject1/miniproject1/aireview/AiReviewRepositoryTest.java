package com.miniproject1.miniproject1.aireview;

import static org.assertj.core.api.Assertions.assertThat;

import com.miniproject1.miniproject1.aireview.AiReviewAnalysis.Condition;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AiReviewRepositoryTest {

    @Autowired
    private AiReviewRepository reviewRepository;

    @Test
    void 검수_결과를_저장하고_이메일_기준_최신순으로_조회한다() {
        AiReview saved = reviewRepository.save(AiReview.builder()
                .email("user@example.com")
                .pblancId("PBLN-1")
                .businessSnapshot(businessProfile())
                .status(AiReviewStatus.MATCHED)
                .resultDetail(analysis())
                .build());

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.isOutdated()).isFalse();

        Page<AiReview> page = reviewRepository.findAllByEmailOrderByCreatedAtDesc(
                "user@example.com", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getResultDetail().conditions().get(0).status())
                .isEqualTo(AiReviewStatus.MATCHED);
        assertThat(page.getContent().get(0).getBusinessSnapshot().region()).isEqualTo("서울");
    }

    @Test
    void 본인_이메일이_아니면_상세_조회되지_않는다() {
        AiReview saved = reviewRepository.save(AiReview.builder()
                .email("owner@example.com")
                .pblancId("PBLN-1")
                .businessSnapshot(businessProfile())
                .status(AiReviewStatus.MATCHED)
                .resultDetail(analysis())
                .build());

        Optional<AiReview> found = reviewRepository.findByIdAndEmail(saved.getId(), "other@example.com");

        assertThat(found).isEmpty();
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

    private AiReviewAnalysis analysis() {
        return new AiReviewAnalysis(
                List.of(new Condition(
                        "지역",
                        AiReviewStatus.MATCHED,
                        "서울",
                        "서울 소재 사업자",
                        "지역 요건을 충족합니다.",
                        "서울 소재 소상공인을 대상으로 합니다.")),
                List.of(),
                List.of());
    }
}
