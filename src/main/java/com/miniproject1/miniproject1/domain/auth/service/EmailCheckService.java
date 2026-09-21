package com.miniproject1.miniproject1.domain.auth.service;

import org.springframework.stereotype.Service;

import com.miniproject1.miniproject1.domain.auth.dto.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.domain.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailCheckService {

    private final UserRepository userRepository;

    public EmailCheckResponseDTO checkEmail(String email) {
        boolean available = !userRepository.existsByEmail(email);
        return EmailCheckResponseDTO.of(available);
    }
}
