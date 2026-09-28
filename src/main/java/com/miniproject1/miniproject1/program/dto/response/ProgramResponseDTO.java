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

    public static ProgramResponseDTO from(Program program) {
        return ProgramResponseDTO.builder()
                .pblancId(program.getPblancId())
                .title(program.getTitle())
                .category(program.getCategory())
                .organization(program.getOrganization())
                .targetDescription(program.getTargetDescription())
                .description(program.getDescription())
                .rawApplyPeriod(program.getRawApplyPeriod())
                .applyStartDate(program.getApplyStartDate())
                .applyEndDate(program.getApplyEndDate())
                .apiUpdatedAt(program.getApiUpdatedAt())
                .build();
    }
}