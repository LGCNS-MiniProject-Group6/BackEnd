package com.miniproject1.miniproject1.auth.service.findemail;

import com.miniproject1.miniproject1.auth.dto.request.findemail.FindEmailSendCodeRequest;
import com.miniproject1.miniproject1.auth.dto.request.findemail.FindEmailVerifyRequest;
import com.miniproject1.miniproject1.auth.dto.response.findemail.FindEmailResponse;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindEmailService {

    private final UserRepository userRepository;
    private final SmsService smsService;

    public void sendCode(FindEmailSendCodeRequest request) {
        // 가입 여부는 응답으로 노출하지 않고, 가입된 번호에만 실제 SMS를 보냅니다.
        if (!userRepository.findAllByPhoneNumber(request.phone()).isEmpty()) {
            smsService.sendVerificationCode(request.phone());
        }
    }

    public FindEmailResponse verify(FindEmailVerifyRequest request) {
        smsService.verifyCode(request.phone(), request.code());

        List<User> users = userRepository.findAllByPhoneNumber(request.phone());
        if (users.size() != 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND,
                    "해당 휴대폰 번호로 가입된 사용자를 찾을 수 없습니다.");
        }

        // 전화번호 인증을 통과한 경우에만 전체 이메일을 반환합니다.
        return new FindEmailResponse(users.get(0).getEmail());
    }
}
