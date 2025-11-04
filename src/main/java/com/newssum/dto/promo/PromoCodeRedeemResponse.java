package com.newssum.dto.promo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.newssum.domain.PromoCode;

import lombok.Builder;

/**
 * Response returned after a successful promo code redemption.
 */
@Builder
public record PromoCodeRedeemResponse(
    String code,
    boolean premiumActivated,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expiresAt
) {
    public static PromoCodeRedeemResponse of(final PromoCode promoCode) {
        return PromoCodeRedeemResponse.builder()
            .code(promoCode.getCode())
            .premiumActivated(true)
            .expiresAt(promoCode.getExpiresAt())
            .build();
    }
}
