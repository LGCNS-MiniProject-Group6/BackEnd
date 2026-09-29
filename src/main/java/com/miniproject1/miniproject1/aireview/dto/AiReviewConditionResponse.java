package com.miniproject1.miniproject1.aireview.dto;

import com.miniproject1.miniproject1.aireview.AiReviewAnalysis.Condition;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;

public record AiReviewConditionResponse(
        String condition,
        AiReviewStatus status,
        String userValue,
        String requirement,
        String reason,
        String evidence) {

    public static AiReviewConditionResponse from(Condition condition) {
        return new AiReviewConditionResponse(
                condition.condition(),
                condition.status(),
                condition.userValue(),
                condition.requirement(),
                condition.reason(),
                condition.evidence());
    }
}
