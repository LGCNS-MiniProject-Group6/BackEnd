package com.miniproject1.miniproject1.chat.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiProgramChatClientTest {

    @Test
    void returnsClearErrorWhenApiKeyIsMissing() {
        OpenAiProgramChatClient client = new OpenAiProgramChatClient(
                RestClient.builder().build(), "", "gpt-4o-mini");

        assertThatThrownBy(() -> client.answer("공고", "사업정보 없음", List.of(), "질문"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_SERVICE_NOT_CONFIGURED));
    }

    @Test
    void convertsOpenAiHttpErrorToExistingBackendErrorFormat() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiProgramChatClient client = new OpenAiProgramChatClient(
                builder.build(), "test-key", "gpt-4o-mini");
        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.answer("공고", "사업정보 없음", List.of(), "질문"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));

        server.verify();
    }
}
