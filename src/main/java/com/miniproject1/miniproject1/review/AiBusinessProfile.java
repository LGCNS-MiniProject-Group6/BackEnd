package com.miniproject1.miniproject1.review;

import java.time.LocalDate;
import java.util.List;

/** AI 검수에 필요한 최소 사업정보 스냅샷입니다. */
public record AiBusinessProfile(
        String region,
        String industry,
        LocalDate openingDate,
        String businessType,
        int employeeCount,
        long annualRevenue,
        List<String> interestCategories
) {

    public AiBusinessProfile {
        interestCategories = interestCategories == null
                ? List.of()
                : List.copyOf(interestCategories);
    }
}
