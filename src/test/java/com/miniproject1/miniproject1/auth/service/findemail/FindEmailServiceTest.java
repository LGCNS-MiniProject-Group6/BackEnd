package com.miniproject1.miniproject1.auth.service.findemail;

import com.miniproject1.miniproject1.auth.dto.request.findemail.FindEmailVerifyRequest;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.user.entity.User;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FindEmailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private FindEmailService findEmailService;

    @Test
    void 가입된_전화번호에만_아이디찾기_문자를_보낸다() {
        String phone = "01012345678";
        when(userRepository.findAllByPhoneNumber(phone)).thenReturn(List.of(User.builder()
                .email("user@example.com")
                .phoneNumber(phone)
                .build()));

        findEmailService.sendCode(new com.miniproject1.miniproject1.auth.dto.request.findemail.FindEmailSendCodeRequest(phone));

        verify(smsService).sendVerificationCode(phone);
    }

    @Test
    void 가입되지_않은_전화번호에는_아이디찾기_문자를_보내지_않는다() {
        String phone = "01012345678";
        when(userRepository.findAllByPhoneNumber(phone)).thenReturn(List.of());

        findEmailService.sendCode(new com.miniproject1.miniproject1.auth.dto.request.findemail.FindEmailSendCodeRequest(phone));

        verify(smsService, never()).sendVerificationCode(phone);
    }

    @Test
    void 전화번호_인증이_완료되면_전체_이메일을_반환한다() {
        String phone = "01012345678";
        User user = User.builder()
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .phoneNumber(phone)
                .build();
        when(userRepository.findAllByPhoneNumber(phone)).thenReturn(List.of(user));

        var response = findEmailService.verify(new FindEmailVerifyRequest(phone, "123456"));

        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void 인증된_전화번호로_사용자를_찾지_못하면_이메일을_반환하지_않는다() {
        String phone = "01012345678";
        when(userRepository.findAllByPhoneNumber(phone)).thenReturn(List.of());

        assertThatThrownBy(() -> findEmailService.verify(new FindEmailVerifyRequest(phone, "123456")))
                .isInstanceOf(RuntimeException.class);
    }
}
