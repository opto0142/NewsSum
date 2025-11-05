package com.newssum.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
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
import com.newssum.external.gemini.GeminiApiClient;
import com.newssum.external.gemini.GeminiApiClient.SummaryResult;
import com.newssum.external.gemini.GeminiApiClient.TranslationResult;
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
    private final GeminiApiClient geminiApiClient;

    public NewsCrawlingService(final NewsCrawler newsCrawler,
        final NewsArticleRepository newsArticleRepository,
        final GeminiApiClient geminiApiClient) {
        this.newsCrawler = newsCrawler;
        this.newsArticleRepository = newsArticleRepository;
        this.geminiApiClient = geminiApiClient;
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
            final NewsArticle saved = newsArticleRepository.save(processArticle(article, urlHash, requesterEmail));
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

    private NewsArticle processArticle(final CrawledArticle article, final String urlHash, final String requesterEmail) {
        final boolean needsTranslation = shouldTranslate(article.getLanguage());
        TranslationResult translationResult = null;
        if (needsTranslation) {
            translationResult = geminiApiClient.translate(article.getTitle(), article.getContent());
        }

        final String summaryTitle = translationResult != null && !translationResult.translatedTitle().isBlank()
            ? translationResult.translatedTitle()
            : article.getTitle();
        final String summaryContent = translationResult != null && !translationResult.translatedContent().isBlank()
            ? translationResult.translatedContent()
            : article.getContent();

        final SummaryResult summaryResult = geminiApiClient.summarize(summaryTitle, summaryContent);

        return NewsArticle.builder()
            .url(article.getUrl())
            .urlHash(urlHash)
            .sourceOutlet(article.getSourceOutlet())
            .originalTitle(article.getTitle())
            .originalContent(article.getContent())
            .language(article.getLanguage())
            .translatedTitle(translationResult != null ? translationResult.translatedTitle() : summaryTitle)
            .translatedContent(translationResult != null ? translationResult.translatedContent() : summaryContent)
            .summary(summaryResult.summary())
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

    private boolean shouldTranslate(final String languageCode) {
        return languageCode == null || !Locale.KOREAN.getLanguage().equalsIgnoreCase(languageCode);
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
