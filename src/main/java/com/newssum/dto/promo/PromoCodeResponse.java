package com.newssum.dto.promo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.newssum.domain.PromoCode;

/**
 * Promo code information shared with administrators.
 */
public record PromoCodeResponse(
    String code,
    boolean active,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expiresAt,
    String usedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime usedAt,
    String createdByAdmin
) {
    public static PromoCodeResponse from(final PromoCode promoCode) {
        return new PromoCodeResponse(
            promoCode.getCode(),
            promoCode.isActive(),
            promoCode.getExpiresAt(),
            promoCode.getUsedBy(),
            promoCode.getUsedAt(),
            promoCode.getCreatedByAdmin()
        );
    }
}
