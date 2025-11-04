package com.newssum.dto.promo;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for redeeming a promo code.
 */
public record PromoCodeUseRequest(
    @NotBlank(message = "프로모션 코드를 입력해주세요.")
    String code
) {
}
