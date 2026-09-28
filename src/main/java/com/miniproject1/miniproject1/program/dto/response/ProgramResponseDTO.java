package com.miniproject1.miniproject1.program.dto.response;

import com.miniproject1.miniproject1.program.entity.Program;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProgramResponseDTO {

    private String pblancId;
    private String title;
    private String category;
    private String organization;
    private String targetDescription;
    private String description;
    private String rawApplyPeriod;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private String apiUpdatedAt;

    private String pblancUrl;

    public static ProgramResponseDTO from(Program entity) {
        return ProgramResponseDTO.builder()
                .pblancId(entity.getPblancId())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .organization(entity.getOrganization())
                .targetDescription(entity.getTargetDescription())
                .description(entity.getDescription())
                .rawApplyPeriod(entity.getRawApplyPeriod())
                .applyStartDate(entity.getApplyStartDate())
                .applyEndDate(entity.getApplyEndDate())
                .apiUpdatedAt(entity.getApiUpdatedAt())
                .pblancUrl(entity.getPblancUrl())
                .build();
    }
}