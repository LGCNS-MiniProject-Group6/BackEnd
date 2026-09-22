package com.miniproject1.miniproject1.auth.dto.signup.response;

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
