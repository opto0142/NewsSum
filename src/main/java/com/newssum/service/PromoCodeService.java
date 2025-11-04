package com.newssum.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newssum.domain.PromoCode;
import com.newssum.dto.promo.PromoCodeCreateRequest;
import com.newssum.dto.promo.PromoCodeRedeemResponse;
import com.newssum.dto.promo.PromoCodeResponse;
import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.PromoCodeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Encapsulates promo code creation, validation, and redemption logic.
 */
@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private static final ZoneOffset DEFAULT_ZONE = ZoneOffset.UTC;

    private final PromoCodeRepository promoCodeRepository;
    private final UserService userService;

    @Transactional
    public PromoCodeRedeemResponse redeemPromoCode(final String code, final String userEmail) {
        final PromoCode promoCode = promoCodeRepository.findByCode(code)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROMO_CODE_NOT_FOUND));

        validatePromoCode(promoCode);

        final LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        userService.activatePremium(userEmail, code);

        promoCode.markUsed(userEmail, now);
        promoCodeRepository.save(promoCode);

        return PromoCodeRedeemResponse.of(promoCode);
    }

    @Transactional
    public PromoCodeResponse createPromoCode(final PromoCodeCreateRequest request, final String adminEmail) {
        if (promoCodeRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.PROMO_CODE_DUPLICATED);
        }

        final PromoCode promoCode = PromoCode.builder()
            .code(request.code())
            .expiresAt(request.expiresAt())
            .createdByAdmin(adminEmail)
            .build();

        return PromoCodeResponse.from(promoCodeRepository.save(promoCode));
    }

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getPromoCodes(final boolean activeOnly) {
        final LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        return promoCodeRepository.findAll().stream()
            .filter(code -> !activeOnly || (code.isActive() && !code.isExpired(now)))
            .map(PromoCodeResponse::from)
            .toList();
    }

    private void validatePromoCode(final PromoCode promoCode) {
        final LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        if (!promoCode.isActive()) {
            throw new BusinessException(ErrorCode.PROMO_CODE_INACTIVE);
        }
        if (promoCode.isExpired(now)) {
            throw new BusinessException(ErrorCode.PROMO_CODE_EXPIRED);
        }
    }
}
