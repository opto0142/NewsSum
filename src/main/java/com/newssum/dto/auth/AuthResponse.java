package com.newssum.dto.auth;

import lombok.Builder;

/**
 * Authentication response containing generated JWT tokens.
 */
@Builder
public record AuthResponse(
    String tokenType,
    String accessToken,
    long expiresIn,
    String refreshToken,
    long refreshExpiresIn
) {
    private static final String BEARER = "Bearer";

    public static AuthResponse of(final String accessToken, final String refreshToken, final long expiresInSeconds,
        final long refreshExpiresInSeconds) {
        return AuthResponse.builder()
            .tokenType(BEARER)
            .accessToken(accessToken)
            .expiresIn(expiresInSeconds)
            .refreshToken(refreshToken)
            .refreshExpiresIn(refreshExpiresInSeconds)
            .build();
    }
}
