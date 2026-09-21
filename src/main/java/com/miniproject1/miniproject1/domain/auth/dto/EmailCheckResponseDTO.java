package com.miniproject1.miniproject1.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailCheckResponseDTO {

    private boolean available;

    public static EmailCheckResponseDTO of(boolean available) {
        return EmailCheckResponseDTO.builder()
                .available(available)
                .build();
    }
}
