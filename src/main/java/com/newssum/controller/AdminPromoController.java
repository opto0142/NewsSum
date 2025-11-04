package com.newssum.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newssum.dto.common.ApiResponse;
import com.newssum.dto.promo.PromoCodeCreateRequest;
import com.newssum.dto.promo.PromoCodeResponse;
import com.newssum.security.UserPrincipal;
import com.newssum.service.PromoCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Provides administrator endpoints for managing promo codes.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/promo")
public class AdminPromoController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCode(
        @Valid @RequestBody final PromoCodeCreateRequest request,
        @AuthenticationPrincipal final UserPrincipal adminPrincipal) {

        final PromoCodeResponse response = promoCodeService.createPromoCode(request, adminPrincipal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "프로모션 코드가 생성되었습니다."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getPromoCodes(
        @RequestParam(value = "activeOnly", defaultValue = "true") final boolean activeOnly) {

        final List<PromoCodeResponse> promoCodes = promoCodeService.getPromoCodes(activeOnly);
        return ResponseEntity.ok(ApiResponse.success(promoCodes));
    }
}
