package com.miniproject1.miniproject1.user.controller;

import com.miniproject1.miniproject1.commons.exception.ErrorResponse;
import com.miniproject1.miniproject1.user.dto.mypage.request.ProfileUpdateRequest;
import com.miniproject1.miniproject1.user.dto.mypage.response.ProfileResponse;
import com.miniproject1.miniproject1.user.service.mypage.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 정보 관리 API")
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final MyPageService myPageService;

    @Operation(summary = "내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "user@example.com",
                                      "name": "박운영",
                                      "phone": "01012345678"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "USER_NOT_FOUND",
                                      "message": "존재하지 않는 사용자입니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/auth/users/me",
                                      "details": []
                                    }
                                    """)))
    })
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(myPageService.getMyProfile(authentication.getName()));
    }

    @Operation(summary = "내 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "user@example.com",
                                      "name": "박운영",
                                      "phone": "01012345678"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "INVALID_INPUT_VALUE",
                                      "message": "입력값 또는 요청 형식이 올바르지 않습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/auth/users/me",
                                      "details": [
                                        { "field": "name", "value": "", "reason": "이름은 필수입니다." }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "USER_NOT_FOUND",
                                      "message": "존재하지 않는 사용자입니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/auth/users/me",
                                      "details": []
                                    }
                                    """)))
    })
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(myPageService.updateMyProfile(authentication.getName(), request));
    }
}
