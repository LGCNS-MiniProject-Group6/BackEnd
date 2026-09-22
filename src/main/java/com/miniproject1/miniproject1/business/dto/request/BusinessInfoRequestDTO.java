package com.miniproject1.miniproject1.business.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class BusinessInfoRequestDTO {
    @Schema (description = "업종", example = "제조업")
    @NotBlank (message = "업종은 필수입니다.")
    private String industry;

    @Schema (description = "지역", example = "서울")
    @NotBlank (message = "지역은 필수입니다.")
    private String region;

    @Schema (description = "개업일", example = "2026-09-22")
    @NotNull (message = "개업일은 필수입니다.")
    private LocalDate openingDate;

    @Schema (description = "직원 수", example = "5")
    @NotNull (message = "직원 수는 필수입니다.")
    @PositiveOrZero (message = "직원 수는 0 이상이어야 합니다.")
    private Integer employeeCount;

    @Schema (description = "연매출", example = "500000000")
    @NotNull (message = "연매출은 필수입니다.")
    @PositiveOrZero (message = "연매출은 0 이상이어야 합니다.")
    private Long annualRevenue;
}
