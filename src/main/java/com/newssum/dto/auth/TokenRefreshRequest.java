package com.newssum.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request payload.
 */
public record TokenRefreshRequest(
    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    String refreshToken
) {
}
