package com.miniproject1.miniproject1.aireview.dto;

import com.miniproject1.miniproject1.aireview.AiReview;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import java.time.LocalDateTime;

public record AiReviewSummaryResponse(
        Long reviewId,
        String pblancId,
        AiReviewStatus status,
        LocalDateTime createdAt) {

    public static AiReviewSummaryResponse from(AiReview review) {
        return new AiReviewSummaryResponse(
                review.getId(),
                review.getPblancId(),
                review.getStatus(),
                review.getCreatedAt());
    }
}
