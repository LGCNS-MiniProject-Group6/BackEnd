package com.miniproject1.miniproject1.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutIntegrationTest {

    private static final String EMAIL = "login@example.com";
    private static final String PASSWORD = "Password123!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    StringRedisTemplate redisTemplate;

    private ValueOperations<String, String> valueOperations;
    private Map<String, String> redisStore;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        userRepository.deleteAll();
        userRepository.saveAndFlush(new User(
                EMAIL,
                passwordEncoder.encode(PASSWORD),
                "테스트 사용자",
                "01012345678"
        ));

        redisStore = new ConcurrentHashMap<>();
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            redisStore.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(valueOperations.get(anyString()))
                .thenAnswer(invocation -> redisStore.get(invocation.getArgument(0)));
        when(redisTemplate.delete(anyString()))
                .thenAnswer(invocation -> redisStore.remove(invocation.getArgument(0)) != null);
    }

    @Test
    void loginThenLogoutRevokesRefreshToken() throws Exception {
        JsonNode loginResponse = login();
        assertThat(redisStore).hasSize(1);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", loginResponse.get("refreshToken").asText()
                        ))))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(redisStore).isEmpty();
    }

    @Test
    void repeatedLogoutIsIdempotent() throws Exception {
        String refreshToken = login().get("refreshToken").asText();
        String body = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidRefreshTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"not-a-jwt\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("INVALID_TOKEN"));
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() throws Exception {
        String accessToken = login().get("accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", accessToken
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("INVALID_TOKEN"));
    }

    @Test
    void blankRefreshTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void correctCredentialsIssueTokensAndStoreRefreshToken() throws Exception {
        JsonNode response = login();

        assertThat(response.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(response.get("accessTokenExpiresIn").asLong()).isEqualTo(1_800_000);
        assertThat(response.get("refreshTokenExpiresIn").asLong()).isEqualTo(604_800_000);
        assertThat(redisStore).hasSize(1).containsKey("RT:" + EMAIL)
                .containsValue(response.get("refreshToken").asText());
    }

    @Test
    void wrongPasswordAndMissingUserReturnSame401() throws Exception {
        for (Map<String, String> request : List.of(
                Map.of("email", EMAIL, "password", "wrong-password"),
                Map.of("email", "missing@example.com", "password", PASSWORD)
        )) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("code").value("AUTH_INVALID_CREDENTIALS"));
        }
    }

    @Test
    void accessTokenAuthenticatesProtectedApi() throws Exception {
        String accessToken = login().get("accessToken").asText();

        mockMvc.perform(get("/protected")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userEmail").value(EMAIL))
                .andExpect(jsonPath("authorities").isEmpty());
    }

    @Test
    void refreshTokenCannotAuthenticateProtectedApi() throws Exception {
        String refreshToken = login().get("refreshToken").asText();

        mockMvc.perform(get("/protected")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void redisFailureDoesNotReturnSuccessfulLogin() throws Exception {
        doThrow(new RedisConnectionFailureException("test Redis failure"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginBody()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("accessToken").doesNotExist());
    }

    private JsonNode login() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").isNotEmpty())
                .andExpect(jsonPath("refreshToken").isNotEmpty())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String validLoginBody() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "email", EMAIL,
                "password", PASSWORD
        ));
    }
}
