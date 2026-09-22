package com.miniproject1.miniproject1.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MyPageIntegrationTest {

    private static final String EMAIL = "mypage@example.com";
    private static final String ACCESS_TOKEN = "Bearer ";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.saveAndFlush(new User(
                EMAIL,
                passwordEncoder.encode("Password123!"),
                "기존 이름",
                "01011112222"));
    }

    @Test
    void getMyProfileUsesEmailFromAccessToken() throws Exception {
        mockMvc.perform(get("/api/auth/users/me")
                        .header("Authorization", ACCESS_TOKEN + jwtTokenProvider.createAccessToken(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value("기존 이름"))
                .andExpect(jsonPath("phone").value("01011112222"))
                .andExpect(jsonPath("userId").doesNotExist());
    }

    @Test
    void updateMyProfileChangesNameAndPhone() throws Exception {
        mockMvc.perform(put("/api/auth/users/me")
                        .header("Authorization", ACCESS_TOKEN + jwtTokenProvider.createAccessToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "변경된 이름",
                                "phone", "01099998888"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(EMAIL))
                .andExpect(jsonPath("name").value("변경된 이름"))
                .andExpect(jsonPath("phone").value("01099998888"));
    }

    @Test
    void myProfileRequiresAccessToken() throws Exception {
        mockMvc.perform(get("/api/auth/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateRequiresNameAndPhone() throws Exception {
        mockMvc.perform(put("/api/auth/users/me")
                        .header("Authorization", ACCESS_TOKEN + jwtTokenProvider.createAccessToken(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"변경된 이름\"}"))
                .andExpect(status().isBadRequest());
    }
}
