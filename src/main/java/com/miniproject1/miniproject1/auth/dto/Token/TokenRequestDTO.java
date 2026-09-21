package com.miniproject1.miniproject1.auth.dto.Token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "토큰 요청 DTO")
public class TokenRequestDTO {

    @Schema(description = "Refresh Token 값", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh Token은 필수 입력값입니다.")
    private String refreshToken;
}