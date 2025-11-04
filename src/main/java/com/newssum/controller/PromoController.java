package com.newssum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newssum.dto.common.ApiResponse;
import com.newssum.dto.promo.PromoCodeRedeemResponse;
import com.newssum.dto.promo.PromoCodeUseRequest;
import com.newssum.security.UserPrincipal;
import com.newssum.service.PromoCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Handles user-facing promo code operations.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promo")
public class PromoController {

    private final PromoCodeService promoCodeService;

    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PromoCodeRedeemResponse>> redeemPromoCode(
        @Valid @RequestBody final PromoCodeUseRequest request,
        @AuthenticationPrincipal final UserPrincipal principal) {

        final PromoCodeRedeemResponse response = promoCodeService.redeemPromoCode(request.code(), principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "프리미엄 전환이 완료되었습니다."));
    }
}
