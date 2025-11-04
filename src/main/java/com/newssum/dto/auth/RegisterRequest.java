package com.newssum.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration payload for creating a new user account.
 */
public record RegisterRequest(
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    String password,

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 30, message = "닉네임은 30자를 넘을 수 없습니다.")
    String nickname
) {
}
