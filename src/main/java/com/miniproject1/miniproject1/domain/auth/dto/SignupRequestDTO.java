package com.miniproject1.miniproject1.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequestDTO {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호 (8자 이상, 영문/숫자/특수문자 포함)", example = "Password123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자를 포함해야 합니다.")
    private String password;

    @Schema(description = "이름", example = "박운영")
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @Schema(description = "휴대폰 번호", example = "01012345678")
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phone;
}
