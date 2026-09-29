package com.miniproject1.miniproject1.review.common.context;

import java.time.LocalDate;

/** AI 검수에 사용하는 공고 기본 정보와 AI 요약입니다. */
public record AiProgramContext(
        String pblancId,
        String title,
        String category,
        String organization,
        String targetDescription,
        String description,
        String rawApplyPeriod,
        LocalDate applyStartDate,
        LocalDate applyEndDate,
        AiProgramSummary summary
) {
}
