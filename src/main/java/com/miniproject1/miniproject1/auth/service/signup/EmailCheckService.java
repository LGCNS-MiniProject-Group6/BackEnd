package com.miniproject1.miniproject1.auth.service.signup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniproject1.miniproject1.auth.dto.signup.response.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailCheckService {

    private final UserRepository userRepository;

    public EmailCheckResponseDTO checkEmail(String email) {
        boolean available = !userRepository.existsByEmail(email);
        return EmailCheckResponseDTO.of(available);
    }
}
