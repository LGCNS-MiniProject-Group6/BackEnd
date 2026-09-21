package com.miniproject1.miniproject1.user.controller;

import com.miniproject1.miniproject1.user.dto.login.LoginRequest;
import com.miniproject1.miniproject1.user.dto.login.LoginResponse;
import com.miniproject1.miniproject1.user.dto.logout.LogoutRequest;
import com.miniproject1.miniproject1.user.service.login.LoginService;
import com.miniproject1.miniproject1.user.service.logout.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;
    private final LogoutService logoutService;

    @Operation(summary = "이메일과 비밀번호로 로그인")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @Operation(summary = "Refresh Token 폐기 및 로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request);
    }
}
