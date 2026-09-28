package com.miniproject1.miniproject1.auth.dto.response.findemail;

import io.swagger.v3.oas.annotations.media.Schema;

public record FindEmailResponse(
        @Schema(description = "전화번호 인증이 완료된 사용자의 이메일")
        String email
) {
}
