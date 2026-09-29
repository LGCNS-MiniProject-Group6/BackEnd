package com.miniproject1.miniproject1.business.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List; // [추가된 import]

import com.miniproject1.miniproject1.business.entity.BusinessInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder 
public class BusinessInfoResponseDTO {
    
    private Long businessId;
    private String companyName;
    private String industry;
    private String region;
    private LocalDate openingDate;
    private String businessType;
    private Integer employeeCount;
    private Long annualRevenue;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= [추가된 필드] =================
    private List<String> interestCategories;
    // =================================================
    // [기존 메서드 - 카테고리 없이 호캐 시 호환성 유지]
    public static BusinessInfoResponseDTO from (BusinessInfo businessInfo) {
        return from(businessInfo, List.of());
    }

    // ================= [추가된 패터리 메서드: 카테고리 포함하여 반환] =================
    public static BusinessInfoResponseDTO from (BusinessInfo businessInfo, List<String> interestCategories) {
        return BusinessInfoResponseDTO.builder()
                .businessId(businessInfo.getBusinessId())
                .companyName(businessInfo.getCompanyName())
                .industry(businessInfo.getIndustry())
                .region(businessInfo.getRegion())
                .openingDate(businessInfo.getOpeningDate())
                .businessType(businessInfo.getBusinessType())
                .employeeCount(businessInfo.getEmployeeCount())
                .annualRevenue(businessInfo.getAnnualRevenue())
                .isDefault(businessInfo.getIsDefault())
                .createdAt(businessInfo.getCreatedAt())
                .updatedAt(businessInfo.getUpdatedAt())
                .interestCategories(interestCategories != null ? interestCategories : List.of())
                .build();
    }
    // ==================================================================
}
