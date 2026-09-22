package com.miniproject1.miniproject1.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.miniproject1.miniproject1.domain.auth.dto.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.domain.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailCheckServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailCheckService emailCheckService;

    @Test
    void 가입되지_않은_이메일이면_사용가능으로_응답한다() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        EmailCheckResponseDTO response = emailCheckService.checkEmail("new@example.com");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    void 이미_가입된_이메일이면_사용불가로_응답한다() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        EmailCheckResponseDTO response = emailCheckService.checkEmail("taken@example.com");

        assertThat(response.isAvailable()).isFalse();
    }
}
