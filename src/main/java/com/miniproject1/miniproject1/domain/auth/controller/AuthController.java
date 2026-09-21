package com.miniproject1.miniproject1.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miniproject1.miniproject1.domain.auth.dto.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.domain.auth.dto.SignupRequestDTO;
import com.miniproject1.miniproject1.domain.auth.dto.SignupResponseDTO;
import com.miniproject1.miniproject1.domain.auth.service.EmailCheckService;
import com.miniproject1.miniproject1.domain.auth.service.SignupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "01. Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final SignupService signupService;
    private final EmailCheckService emailCheckService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = signupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "이메일 중복확인")
    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDTO> checkEmail(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {
        EmailCheckResponseDTO response = emailCheckService.checkEmail(email);
        return ResponseEntity.ok(response);
    }
}
