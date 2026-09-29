package com.miniproject1.miniproject1.review.dto;

import com.miniproject1.miniproject1.review.entity.Review;
import com.miniproject1.miniproject1.review.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailResponse(
        Long reviewId,
        String pblancId,
        ReviewStatus overallStatus,
        String model,
        String promptVersion,
        String documentHash,
        LocalDateTime createdAt,
        List<ReviewConditionResponse> conditions) {

    public static ReviewDetailResponse from(Review review) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getPblancId(),
                review.getOverallStatus(),
                review.getModel(),
                review.getPromptVersion(),
                review.getDocumentHash(),
                review.getCreatedAt(),
                review.getConditions().stream().map(ReviewConditionResponse::from).toList());
    }
}
