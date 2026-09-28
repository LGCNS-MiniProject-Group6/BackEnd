package com.miniproject1.miniproject1.auth.service.password;

import com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetRequest;
import com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetVerifyRequest;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void 새_비밀번호와_확인이_다르면_저장하지_않는다() {
        PasswordResetRequest request = new PasswordResetRequest(
                "reset-token", "NewPassword123!", "DifferentPassword123!");

        assertThatThrownBy(() -> passwordResetService.reset(request))
                .isInstanceOf(RuntimeException.class);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void 만료된_재설정_토큰은_사용할수없다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("PASSWORD_RESET:expired-token")).thenReturn(null);

        PasswordResetRequest request = new PasswordResetRequest(
                "expired-token", "NewPassword123!", "NewPassword123!");

        assertThatThrownBy(() -> passwordResetService.reset(request))
                .isInstanceOf(RuntimeException.class);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void 인증된_이메일과_전화번호가_일치하는지_확인한다() {
        String email = "user@example.com";
        String phone = "01012345678";
        when(userRepository.findByEmailAndPhoneNumber(email, phone)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.sendCode(
                new com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetSendCodeRequest(email, phone)))
                .isInstanceOf(RuntimeException.class);

        verify(smsService, never()).sendVerificationCode(phone);
    }
}
