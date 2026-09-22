package com.miniproject1.miniproject1.user.dto.mypage.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        @Size(max = 20, message = "휴대폰 번호는 20자 이하로 입력해주세요.")
        String phone
) {
}
