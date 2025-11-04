package com.newssum.service;

import java.time.Duration;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newssum.domain.User;
import com.newssum.dto.auth.AuthResponse;
import com.newssum.dto.auth.LoginRequest;
import com.newssum.dto.auth.RegisterRequest;
import com.newssum.dto.auth.TokenRefreshRequest;
import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.UserRepository;
import com.newssum.security.JwtTokenProvider;
import com.newssum.security.UserPrincipal;

/**
 * Handles authentication workflows including registration, login, and token refresh.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
        final JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(final RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        final User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .nickname(request.nickname())
            .roles(List.of("ROLE_USER"))
            .build();

        userRepository.save(user);
    }

    public AuthResponse login(final LoginRequest request) {
        final User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        final UserPrincipal principal = UserPrincipal.from(user);
        return buildAuthResponse(principal);
    }

    public AuthResponse refreshToken(final TokenRefreshRequest request) {
        final String refreshToken = request.refreshToken();
        jwtTokenProvider.validateToken(refreshToken);
        final String email = jwtTokenProvider.extractEmail(refreshToken);

        final User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        final UserPrincipal principal = UserPrincipal.from(user);
        return buildAuthResponse(principal);
    }

    private AuthResponse buildAuthResponse(final UserPrincipal principal) {
        final String accessToken = jwtTokenProvider.generateAccessToken(principal);
        final String refreshToken = jwtTokenProvider.generateRefreshToken(principal.getUsername());
        final Duration accessValidity = jwtTokenProvider.getAccessTokenValidity();
        final Duration refreshValidity = jwtTokenProvider.getRefreshTokenValidity();
        return AuthResponse.of(accessToken, refreshToken, accessValidity.toSeconds(), refreshValidity.toSeconds());
    }
}
