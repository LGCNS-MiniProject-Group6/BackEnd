package com.miniproject1.miniproject1.chat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 공고 기반 챗봇 질문 요청입니다. */
public record ChatRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        @Size(max = 1_000, message = "질문은 1000자 이하로 입력해주세요.")
        String message,

        @Size(max = 10, message = "이전 대화는 최대 10개까지 전달할 수 있습니다.")
        List<@Valid ChatHistoryMessage> history) {

    public ChatRequest {
        history = history == null ? List.of() : List.copyOf(history);
    }
}
