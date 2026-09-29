package com.miniproject1.miniproject1.review.dto.ai;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;

import java.time.LocalDate;

public record AiReviewRequest(String pblancId, BusinessInfoPayload businessInfo) {

    public static AiReviewRequest from(String pblancId, BusinessInfo info) {
        return new AiReviewRequest(pblancId, new BusinessInfoPayload(
                info.getIndustry(), info.getRegion(), info.getOpeningDate(),
                info.getEmployeeCount(), info.getAnnualRevenue()));
    }

    public record BusinessInfoPayload(
            String industry,
            String region,
            LocalDate openingDate,
            Integer employeeCount,
            Long annualRevenue) {
    }
}
