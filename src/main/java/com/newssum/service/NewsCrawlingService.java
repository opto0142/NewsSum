package com.newssum.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.newssum.crawler.CrawledArticle;
import com.newssum.crawler.NewsCrawler;
import com.newssum.domain.NewsArticle;
import com.newssum.dto.news.CrawlNewsRequest;
import com.newssum.dto.news.CrawledArticleResponse;
import com.newssum.dto.news.CrawlNewsResponse;
import com.newssum.exception.CrawlingException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.NewsArticleRepository;

/**
 * 뉴스 크롤링 요청을 처리하고 MongoDB에 기사 문서를 저장한다.
 */
@Service
public class NewsCrawlingService {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawlingService.class);
    private static final int DEFAULT_ARTICLE_COUNT = 5;

    private final NewsCrawler newsCrawler;
    private final NewsArticleRepository newsArticleRepository;

    public NewsCrawlingService(final NewsCrawler newsCrawler,
        final NewsArticleRepository newsArticleRepository) {
        this.newsCrawler = newsCrawler;
        this.newsArticleRepository = newsArticleRepository;
    }

    public CrawlNewsResponse crawlNews(final CrawlNewsRequest request, final String requesterEmail) {
        final int requestedCount = request.resolveArticleCount(DEFAULT_ARTICLE_COUNT);
        log.info("뉴스 크롤링 요청 수신: url={}, count={}, requester={}", request.getSourceUrl(), requestedCount, requesterEmail);
        final List<String> articleLinks = newsCrawler.extractArticleLinks(request.getSourceUrl(), requestedCount);
        if (articleLinks.isEmpty()) {
            throw new CrawlingException(ErrorCode.CRAWLING_NO_ARTICLE);
        }
        final List<CompletableFuture<Optional<CrawledArticle>>> futures = articleLinks.stream()
            .map(newsCrawler::fetchArticleAsync)
            .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        final List<CrawledArticleResponse> responses = new ArrayList<>();
        int processed = 0;
        int skipped = 0;

        for (CompletableFuture<Optional<CrawledArticle>> future : futures) {
            final Optional<CrawledArticle> optionalArticle = future.join();
            if (optionalArticle.isEmpty()) {
                skipped++;
                continue;
            }
            final CrawledArticle article = optionalArticle.get();
            final String urlHash = hashUrl(article.getUrl());
            final Optional<NewsArticle> existing = newsArticleRepository.findByUrlHash(urlHash);
            if (existing.isPresent()) {
                responses.add(toResponse(existing.get(), false));
                skipped++;
                continue;
            }
            final NewsArticle saved = newsArticleRepository.save(buildNewsArticle(article, urlHash, requesterEmail));
            processed++;
            responses.add(toResponse(saved, true));
        }
        return CrawlNewsResponse.builder()
            .requestedCount(requestedCount)
            .processedCount(processed)
            .skippedCount(skipped)
            .articles(responses)
            .build();
    }

    private NewsArticle buildNewsArticle(final CrawledArticle article, final String urlHash, final String requesterEmail) {
        return NewsArticle.builder()
            .url(article.getUrl())
            .urlHash(urlHash)
            .sourceOutlet(article.getSourceOutlet())
            .originalTitle(article.getTitle())
            .originalContent(article.getContent())
            .language(article.getLanguage())
            .publishedAt(article.getPublishedAt())
            .crawledBy(requesterEmail)
            .crawledAt(LocalDateTime.now())
            .build();
    }

    private CrawledArticleResponse toResponse(final NewsArticle newsArticle, final boolean newlyCreated) {
        return CrawledArticleResponse.builder()
            .id(newsArticle.getId())
            .url(newsArticle.getUrl())
            .sourceOutlet(newsArticle.getSourceOutlet())
            .originalTitle(newsArticle.getOriginalTitle())
            .language(newsArticle.getLanguage())
            .publishedAt(newsArticle.getPublishedAt())
            .newlyCreated(newlyCreated)
            .build();
    }

    private String hashUrl(final String url) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            log.error("SHA-256 해시 생성 실패", ex);
            throw new CrawlingException(ErrorCode.CRAWLING_FAILED, ex);
        }
    }

}
