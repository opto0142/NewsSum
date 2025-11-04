package com.newssum.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.newssum.domain.NewsArticle;

/**
 * MongoDB repository for {@link NewsArticle} documents.
 */
@Repository
public interface NewsArticleRepository extends MongoRepository<NewsArticle, String> {

    Optional<NewsArticle> findByUrlHash(String urlHash);

    boolean existsByUrlHash(String urlHash);
}
