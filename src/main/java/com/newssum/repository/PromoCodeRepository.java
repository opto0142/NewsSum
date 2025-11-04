package com.newssum.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.newssum.domain.PromoCode;

/**
 * MongoDB repository for {@link PromoCode} documents.
 */
@Repository
public interface PromoCodeRepository extends MongoRepository<PromoCode, String> {

    Optional<PromoCode> findByCode(String code);

    Optional<PromoCode> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);

    List<PromoCode> findByActiveTrueAndExpiresAtAfter(LocalDateTime referenceTime);
}
