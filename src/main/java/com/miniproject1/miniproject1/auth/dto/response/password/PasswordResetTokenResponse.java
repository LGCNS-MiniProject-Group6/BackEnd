package com.miniproject1.miniproject1.auth.dto.response.password;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetTokenResponse(
        @Schema(description = "비밀번호 변경에 한 번만 사용할 수 있는 토큰")
        String resetToken
) {
}
