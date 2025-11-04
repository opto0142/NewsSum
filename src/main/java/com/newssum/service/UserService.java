package com.newssum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newssum.domain.User;
import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Manages {@link User} lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getByEmail(final String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public User activatePremium(final String email, final String promoCode) {
        final User user = getByEmail(email);
        if (user.isPremium()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_PREMIUM);
        }
        user.activatePremium(promoCode);
        return userRepository.save(user);
    }

    @Transactional
    public User save(final User user) {
        return userRepository.save(user);
    }
}
