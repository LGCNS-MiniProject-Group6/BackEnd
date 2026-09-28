package com.miniproject1.miniproject1.program.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramAutoCompleteResponseDTO {

    private String pblancId;
    private String title;
    private String organization;

    public static ProgramAutoCompleteResponseDto from(Program program) {
        return ProgramAutoCompleteResponseDto.builder()
                .pblancId(program.getPblancId())
                .title(program.getTitle())
                .organization(program.getOrganization())
                .build();
    }
}
