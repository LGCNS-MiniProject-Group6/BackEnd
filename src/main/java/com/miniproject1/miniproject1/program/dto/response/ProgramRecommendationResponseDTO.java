package com.miniproject1.miniproject1.program.dto.response;

import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import com.miniproject1.miniproject1.program.entity.Program;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/** AI가 사업정보와 공고 요약을 비교해 적합도 순으로 정렬한 추천 공고 응답입니다. */
@Getter
@Builder
public class ProgramRecommendationResponseDTO {

    private String pblancId;
    private String title;
    private String category;
    private String organization;
    private String targetDescription;
    private String description;
    private String rawApplyPeriod;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private String apiUpdatedAt;
    private String pblancUrl;
    private AiReviewStatus reviewStatus;
    private Integer recommendationScore;
    private String matchReason;

    public static ProgramRecommendationResponseDTO from(
            Program program, AiReviewStatus reviewStatus, Integer recommendationScore, String matchReason) {
        return ProgramRecommendationResponseDTO.builder()
                .pblancId(program.getPblancId())
                .title(program.getTitle())
                .category(program.getCategory())
                .organization(program.getOrganization())
                .targetDescription(program.getTargetDescription())
                .description(program.getDescription())
                .rawApplyPeriod(program.getRawApplyPeriod())
                .applyStartDate(program.getApplyStartDate())
                .applyEndDate(program.getApplyEndDate())
                .apiUpdatedAt(program.getApiUpdatedAt())
                .pblancUrl(program.getPblancUrl())
                .reviewStatus(reviewStatus)
                .recommendationScore(recommendationScore)
                .matchReason(matchReason)
                .build();
    }
}
