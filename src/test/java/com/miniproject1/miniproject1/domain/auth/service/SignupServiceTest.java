package com.miniproject1.miniproject1.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.domain.auth.dto.SignupRequestDTO;
import com.miniproject1.miniproject1.domain.auth.dto.SignupResponseDTO;
import com.miniproject1.miniproject1.domain.auth.entity.User;
import com.miniproject1.miniproject1.domain.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    private SignupRequestDTO createRequest() {
        SignupRequestDTO request = new SignupRequestDTO();
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "Password123!");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "phone", "01012345678");
        return request;
    }

    @Test
    void 이메일이_중복되지_않으면_회원가입에_성공한다() {
        SignupRequestDTO request = createRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SignupResponseDTO response = signupService.signup(request);

        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getName()).isEqualTo(request.getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void 이미_가입된_이메일이면_예외가_발생하고_저장되지_않는다() {
        SignupRequestDTO request = createRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        BusinessException exception = catchThrowableOfType(
                BusinessException.class, () -> signupService.signup(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE);
        verify(userRepository, never()).save(any(User.class));
    }
}
