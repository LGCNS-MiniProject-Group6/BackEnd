package com.miniproject1.miniproject1.business.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Integer employeeCount;
    private Long annualRevenue;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BusinessInfoResponseDTO from (BusinessInfo businessInfo) {
        return BusinessInfoResponseDTO.builder()
                .businessId(businessInfo.getBusinessId())
                .companyName(businessInfo.getCompanyName())
                .industry(businessInfo.getIndustry())
                .region(businessInfo.getRegion())
                .openingDate(businessInfo.getOpeningDate())
                .employeeCount(businessInfo.getEmployeeCount())
                .annualRevenue(businessInfo.getAnnualRevenue())
                .isDefault(businessInfo.getIsDefault())
                .createdAt(businessInfo.getCreatedAt())
                .updatedAt(businessInfo.getUpdatedAt())
                .build();
    }
}
