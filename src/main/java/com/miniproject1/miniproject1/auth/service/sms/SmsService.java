package com.miniproject1.miniproject1.auth.service.sms;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsUtil smsUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String SMS_PREFIX = "SMS:";
    private static final long VERIFICATION_TIME = 3 * 60L; // 3분

    // 1. 인증번호 발송
    public void sendVerificationCode(String phoneNumber) {
        String code = createCode();

        // CoolSMS로 문자 전송
        smsUtil.sendSms(phoneNumber, code);

        // Redis에 (Key: SMS:01012345678, Value: 123456, TTL: 5분) 저장
        redisTemplate.opsForValue().set(
                SMS_PREFIX + phoneNumber,
                code,
                Duration.ofSeconds(VERIFICATION_TIME));
    }

    // 2. 인증번호 검증
    public boolean verifyCode(String phoneNumber, String code) {
        String savedCode = redisTemplate.opsForValue().get(SMS_PREFIX + phoneNumber);

        if (savedCode == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "인증번호가 만료되었거나 요청 이력이 없습니다.");
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "인증번호가 일치하지 않습니다.");
        }

        // 검증 성공 시 Redis에서 제거
        redisTemplate.delete(SMS_PREFIX + phoneNumber);
        return true;
    }

    // 6자리 난수 생성기
    private String createCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}