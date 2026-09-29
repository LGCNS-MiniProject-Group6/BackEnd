package com.miniproject1.miniproject1.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 이전 대화 한 건입니다. */
public record ChatHistoryMessage(
        @NotBlank
        @Pattern(regexp = "(?i)user|assistant", message = "role은 user 또는 assistant만 사용할 수 있습니다.")
        String role,

        @NotBlank
        @Size(max = 2_000, message = "이전 메시지는 2000자 이하로 입력해주세요.")
        String content) {
}
