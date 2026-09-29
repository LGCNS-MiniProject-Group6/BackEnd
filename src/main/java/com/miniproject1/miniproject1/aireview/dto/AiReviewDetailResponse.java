package com.miniproject1.miniproject1.aireview.dto;

import com.miniproject1.miniproject1.aireview.AiReview;
import com.miniproject1.miniproject1.aireview.AiReviewAnalysis;
import com.miniproject1.miniproject1.aireview.AiReviewResult;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import java.time.LocalDateTime;
import java.util.List;

public record AiReviewDetailResponse(
        Long reviewId,
        String pblancId,
        AiReviewStatus status,
        LocalDateTime createdAt,
        List<AiReviewConditionResponse> conditions,
        List<AiReviewGroundedTextResponse> warnings,
        List<AiReviewGroundedTextResponse> documents) {

    public static AiReviewDetailResponse from(AiReviewResult result) {
        return of(result.reviewId(), result.pblancId(), result.status(), result.createdAt(), result.detail());
    }

    public static AiReviewDetailResponse from(AiReview review) {
        return of(review.getId(), review.getPblancId(), review.getStatus(), review.getCreatedAt(),
                review.getResultDetail());
    }

    private static AiReviewDetailResponse of(
            long reviewId,
            String pblancId,
            AiReviewStatus status,
            LocalDateTime createdAt,
            AiReviewAnalysis detail) {
        return new AiReviewDetailResponse(
                reviewId,
                pblancId,
                status,
                createdAt,
                detail.conditions().stream().map(AiReviewConditionResponse::from).toList(),
                detail.warnings().stream().map(AiReviewGroundedTextResponse::from).toList(),
                detail.documents().stream().map(AiReviewGroundedTextResponse::from).toList());
    }
}
