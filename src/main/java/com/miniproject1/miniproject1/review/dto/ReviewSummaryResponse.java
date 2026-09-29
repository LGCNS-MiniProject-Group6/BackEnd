package com.miniproject1.miniproject1.review.dto;

import com.miniproject1.miniproject1.review.entity.Review;
import com.miniproject1.miniproject1.review.entity.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
        Long reviewId,
        String pblancId,
        ReviewStatus overallStatus,
        LocalDateTime createdAt) {

    public static ReviewSummaryResponse from(Review review) {
        return new ReviewSummaryResponse(
                review.getId(),
                review.getPblancId(),
                review.getOverallStatus(),
                review.getCreatedAt());
    }
}
