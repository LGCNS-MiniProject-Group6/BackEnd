package com.miniproject1.miniproject1.review.common.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miniproject1.miniproject1.review.common.prompt.AiPrompt;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;

@ExtendWith(MockitoExtension.class)
class SpringAiModelClientTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private SpringAiModelClient modelClient;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        modelClient = new SpringAiModelClient(chatClientBuilder);
    }

    @Test
    void 구조화된_응답을_생성한다() {
        AiPrompt prompt = prepareRequest();
        TestResponse expected = new TestResponse("MATCHED");
        when(responseSpec.entity(TestResponse.class)).thenReturn(expected);

        TestResponse response = modelClient.generateStructured(prompt, TestResponse.class);

        assertThat(response).isEqualTo(expected);
        verify(requestSpec).system("시스템 지시문");
        verify(requestSpec).user("사용자 입력");
    }

    @Test
    void 빈_응답은_외부_API_오류로_처리한다() {
        AiPrompt prompt = prepareRequest();
        when(responseSpec.entity(TestResponse.class)).thenReturn(null);

        assertThatThrownBy(() -> modelClient.generateStructured(prompt, TestResponse.class))
                .isInstanceOf(AiModelCallException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
    }

    @Test
    void 시간초과는_타임아웃_오류로_처리한다() {
        AiPrompt prompt = prepareRequest();
        when(responseSpec.entity(TestResponse.class))
                .thenThrow(new RuntimeException(new SocketTimeoutException("timeout")));

        assertThatThrownBy(() -> modelClient.generateStructured(prompt, TestResponse.class))
                .isInstanceOf(AiModelCallException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXTERNAL_API_TIMEOUT);
    }

    private AiPrompt prepareRequest() {
        AiPrompt prompt = new AiPrompt("시스템 지시문", "사용자 입력");
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(prompt.systemMessage())).thenReturn(requestSpec);
        when(requestSpec.user(prompt.userMessage())).thenReturn(requestSpec);
        when(requestSpec.options(any(OpenAiChatOptions.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        return prompt;
    }

    private record TestResponse(String status) {
    }
}
