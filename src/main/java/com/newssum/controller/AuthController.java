package com.newssum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newssum.dto.auth.AuthResponse;
import com.newssum.dto.auth.LoginRequest;
import com.newssum.dto.auth.RegisterRequest;
import com.newssum.dto.auth.TokenRefreshRequest;
import com.newssum.dto.common.ApiResponse;
import com.newssum.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Handles authentication-related endpoints.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody final RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody final LoginRequest request) {
        final AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인이 완료되었습니다."));
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
        @Valid @RequestBody final TokenRefreshRequest request) {
        final AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
