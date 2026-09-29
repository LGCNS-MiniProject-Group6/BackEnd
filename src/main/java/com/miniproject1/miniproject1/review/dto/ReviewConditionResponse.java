package com.miniproject1.miniproject1.review.dto;

import com.miniproject1.miniproject1.review.entity.ReviewCondition;
import com.miniproject1.miniproject1.review.entity.ReviewConditionStatus;

public record ReviewConditionResponse(
        String conditionType,
        ReviewConditionStatus status,
        String reason,
        Integer page,
        String evidenceText) {

    public static ReviewConditionResponse from(ReviewCondition condition) {
        return new ReviewConditionResponse(
                condition.getConditionType(),
                condition.getStatus(),
                condition.getReason(),
                condition.getPage(),
                condition.getEvidenceText());
    }
}
