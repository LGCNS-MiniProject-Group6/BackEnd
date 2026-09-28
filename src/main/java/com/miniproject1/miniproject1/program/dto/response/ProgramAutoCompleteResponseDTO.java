package com.miniproject1.miniproject1.program.dto.response;

import com.miniproject1.miniproject1.program.entity.Program;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProgramAutoCompleteResponseDTO {

    private String pblancId;
    private String title;
    private String organization;

    public static ProgramAutoCompleteResponseDTO from(Program program) {
        return ProgramAutoCompleteResponseDTO.builder()
                .pblancId(program.getPblancId())
                .title(program.getTitle())
                .organization(program.getOrganization())
                .build();
    }
}