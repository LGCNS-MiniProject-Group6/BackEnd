package com.miniproject1.miniproject1.program.dto.response;

import com.miniproject1.miniproject1.program.entity.AiSummary;
import com.miniproject1.miniproject1.program.entity.Program;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProgramDetailResponseDTO {

    // 1. 공고 기본 정보
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

    // 2. AI 요약 6가지 정보
    private AiSummaryResponseDTO aiSummary;

    public static ProgramDetailResponseDTO of(Program program, AiSummary aiSummary) {
        return ProgramDetailResponseDTO.builder()
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
                .pblancUrl(program.getPblancUrl())
                .aiSummary(AiSummaryResponseDTO.from(aiSummary))
                .build();
    }
}