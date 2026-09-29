package com.miniproject1.miniproject1.review.dto.ai;

import java.util.List;

public record AiReviewResponse(
        String overallStatus,
        String model,
        String promptVersion,
        String documentHash,
        List<Condition> conditions) {

    public record Condition(
            String conditionType,
            String status,
            String reason,
            Integer page,
            String evidenceText) {
    }
}
