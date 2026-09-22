package com.miniproject1.miniproject1.auth.dto.signup.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponseDTO {

    private static final String SIGNUP_SUCCESS_MESSAGE = "회원가입이 완료되었습니다.";

    private String email;
    private String name;
    private String message;

    public static SignupResponseDTO of(String email, String name) {
        return SignupResponseDTO.builder()
                .email(email)
                .name(name)
                .message(SIGNUP_SUCCESS_MESSAGE)
                .build();
    }
}
