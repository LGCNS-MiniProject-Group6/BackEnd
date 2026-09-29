package com.miniproject1.miniproject1.aireview.common.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AiPromptTest {

    @Test
    void 시스템과_사용자_메시지를_분리해_보관한다() {
        AiPrompt prompt = new AiPrompt("시스템 지시문", "사용자 입력");

        assertThat(prompt.systemMessage()).isEqualTo("시스템 지시문");
        assertThat(prompt.userMessage()).isEqualTo("사용자 입력");
    }

    @Test
    void 빈_메시지는_허용하지_않는다() {
        assertThatThrownBy(() -> new AiPrompt(" ", "사용자 입력"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AiPrompt("시스템 지시문", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
