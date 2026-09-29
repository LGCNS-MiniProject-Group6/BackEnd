package com.miniproject1.miniproject1.aireview.common.prompt;

/**
 * AI 모델에 전달할 시스템 지시문과 사용자 입력을 분리해서 보관합니다.
 */
public record AiPrompt(
        String systemMessage,
        String userMessage
) {

    public AiPrompt {
        systemMessage = requireText(systemMessage, "systemMessage");
        userMessage = requireText(userMessage, "userMessage");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
