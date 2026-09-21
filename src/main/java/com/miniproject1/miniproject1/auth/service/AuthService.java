package com.miniproject1.miniproject1.auth.service;

import com.miniproject1.miniproject1.auth.repository.AuthRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // UserRepository 대신 AuthRepository 주입
    private final AuthRepository authRepository;

    /**
     * 이메일 중복 검사
     */
    public boolean checkEmailDuplicate(String email) {
        // 존재하는지 검증
        return !authRepository.existsByEmail(email);

    }
}