package com.miniproject1.miniproject1.review.common.client;

import com.miniproject1.miniproject1.review.common.prompt.AiPrompt;
import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

/** Spring AI의 ChatClient를 사용하는 검수 모델 호출 구현체입니다. */
@Component
public class SpringAiModelClient implements AiModelClient {

    private static final OpenAiChatOptions REVIEW_OPTIONS = OpenAiChatOptions.builder()
            .temperature(0.0)
            .build();

    private final ChatClient chatClient;

    public SpringAiModelClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public <T> T generateStructured(AiPrompt prompt, Class<T> responseType) {
        Objects.requireNonNull(responseType, "responseType must not be null");

        try {
            T response = request(prompt).entity(responseType);

            if (response == null) {
                throw AiModelCallException.emptyResponse();
            }
            return response;
        } catch (AiModelCallException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw AiModelCallException.from(exception);
        }
    }

    /** 시스템 지시문과 사용자 데이터를 서로 다른 메시지 역할로 전달합니다. */
    private ChatClient.CallResponseSpec request(AiPrompt prompt) {
        return chatClient.prompt()
                .system(prompt.systemMessage())
                .user(prompt.userMessage())
                .options(REVIEW_OPTIONS)
                .call();
    }
}
