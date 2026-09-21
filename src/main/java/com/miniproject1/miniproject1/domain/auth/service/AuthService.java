package com.miniproject1.miniproject1.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.domain.auth.dto.SignupRequestDTO;
import com.miniproject1.miniproject1.domain.auth.dto.SignupResponseDTO;
import com.miniproject1.miniproject1.domain.user.entity.User;
import com.miniproject1.miniproject1.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponseDTO signup(SignupRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhone())
                .role(DEFAULT_ROLE)
                .build();

        User saved = userRepository.save(user);

        return SignupResponseDTO.of(saved.getEmail(), saved.getName());
    }
}
