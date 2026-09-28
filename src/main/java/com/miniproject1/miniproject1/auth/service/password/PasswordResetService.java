package com.miniproject1.miniproject1.auth.service.password;

import com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetRequest;
import com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetSendCodeRequest;
import com.miniproject1.miniproject1.auth.dto.request.password.PasswordResetVerifyRequest;
import com.miniproject1.miniproject1.auth.dto.response.password.PasswordResetTokenResponse;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String RESET_TOKEN_PREFIX = "PASSWORD_RESET:";
    private static final long RESET_TOKEN_MINUTES = 5L;

    private final UserRepository userRepository;
    private final SmsService smsService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public void sendCode(PasswordResetSendCodeRequest request) {
        // 존재하지 않는 계정인지 응답으로 노출하지 않습니다. 일치하는 경우에만 SMS를 보냅니다.
        userRepository.findByEmailAndPhoneNumber(request.email(), request.phone())
                .ifPresent(user -> smsService.sendVerificationCode(request.phone()));
    }

    public PasswordResetTokenResponse verifyCode(PasswordResetVerifyRequest request) {
        smsService.verifyCode(request.phone(), request.code());

        User user = userRepository.findByEmailAndPhoneNumber(request.email(), request.phone())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "이메일과 휴대폰 번호가 일치하는 사용자를 찾을 수 없습니다."));

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + resetToken,
                user.getEmail(),
                Duration.ofMinutes(RESET_TOKEN_MINUTES));

        return new PasswordResetTokenResponse(resetToken);
    }

    @Transactional
    public void reset(PasswordResetRequest request) {
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String key = RESET_TOKEN_PREFIX + request.resetToken();
        String email = redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN,
                    "만료되었거나 이미 사용한 비밀번호 재설정 토큰입니다.");
        }

        User user = userRepository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // 비밀번호 변경 후 기존 Refresh Token을 폐기하여 기존 세션을 종료합니다.
        redisTemplate.delete("RT:" + email);
        redisTemplate.delete(key);
    }
}
