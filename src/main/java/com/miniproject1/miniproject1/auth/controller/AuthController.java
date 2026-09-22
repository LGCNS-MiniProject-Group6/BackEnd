package com.miniproject1.miniproject1.auth.controller;

import java.util.Map;

import com.miniproject1.miniproject1.auth.dto.Token.TokenRequestDTO;
import com.miniproject1.miniproject1.auth.dto.Token.TokenResponseDTO;
import com.miniproject1.miniproject1.auth.dto.login.LoginRequest;
import com.miniproject1.miniproject1.auth.dto.login.LoginResponse;
import com.miniproject1.miniproject1.auth.dto.logout.LogoutRequest;
import com.miniproject1.miniproject1.auth.dto.mypage.request.ProfileUpdateRequest;
import com.miniproject1.miniproject1.auth.dto.mypage.response.ProfileResponse;
import com.miniproject1.miniproject1.auth.dto.signup.request.SignupRequestDTO;
import com.miniproject1.miniproject1.auth.dto.signup.response.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.auth.dto.signup.response.SignupResponseDTO;
import com.miniproject1.miniproject1.auth.service.login.LoginService;
import com.miniproject1.miniproject1.auth.service.logout.LogoutService;
import com.miniproject1.miniproject1.auth.service.mypage.MyPageService;
import com.miniproject1.miniproject1.auth.service.signup.EmailCheckService;
import com.miniproject1.miniproject1.auth.service.signup.SignupService;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.auth.service.token.TokenService;
import com.miniproject1.miniproject1.commons.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 / 로그인 / 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {
    private final EmailCheckService emailCheckService;
    private final SmsService smsService;
    private final SignupService signupService;
    private final LoginService loginService;
    private final TokenService tokenService;
    private final LogoutService logoutService;
    private final MyPageService myPageService;

    @Operation(summary = "1. 이메일 중복확인")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "이메일 형식이 올바르지 않거나 비어있음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDTO> checkEmail(
            @RequestParam("email") @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {
        return ResponseEntity.ok(emailCheckService.checkEmail(email));
    }

    @Operation(summary = "2. SMS 인증번호 발송")
    @PostMapping("/phone-verification/send")
    public ResponseEntity<?> sendSmsCode(@RequestParam("phoneNumber") String phoneNumber) {
        smsService.sendVerificationCode(phoneNumber);
        return ResponseEntity.ok(Map.of("message", "인증번호 발송 성공 (Redis 저장 완료)"));
    }

    @Operation(summary = "3. SMS 인증번호 검증")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "인증번호가 일치하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인증번호가 만료되었거나 요청 이력이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/phone-verification/verify")
    public ResponseEntity<?> verifySmsCode(@RequestParam("phoneNumber") String phoneNumber,
                                           @RequestParam("code") String code) {
        boolean result = smsService.verifyCode(phoneNumber, code);
        return ResponseEntity.ok(Map.of("message", "인증 성공!", "isVerified", result));
    }

    @Operation(summary = "4. 회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signupService.signup(request));
    }

    @Operation(summary = "5. 이메일과 비밀번호로 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @Operation(summary = "6. 리프레시 토큰 재발급(RTR)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDTO> reissue(@Valid @RequestBody TokenRequestDTO request) {
        return ResponseEntity.ok(tokenService.reissue(request));
    }

    @Operation(summary = "7. Refresh Token 폐기 및 로그아웃")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request);
    }

    @Operation(summary = "8. 내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(myPageService.getMyProfile(authentication.getName()));
    }

    @Operation(summary = "9. 내 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/users/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(Authentication authentication,
                                                            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(myPageService.updateMyProfile(authentication.getName(), request));
    }
}
