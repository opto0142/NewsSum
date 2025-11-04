package com.newssum.dto.promo;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for admin promo code creation.
 */
public record PromoCodeCreateRequest(
    @NotBlank(message = "프로모션 코드를 입력해주세요.")
    String code,

    @Future(message = "만료일은 미래 시점이어야 합니다.")
    LocalDateTime expiresAt
) {
}
