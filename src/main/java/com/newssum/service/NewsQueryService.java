package com.newssum.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.newssum.domain.NewsArticle;
import com.newssum.dto.news.NewsArticleDetailResponse;
import com.newssum.dto.news.NewsHistoryItemResponse;
import com.newssum.dto.news.NewsHistoryResponse;
import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.NewsArticleRepository;

import lombok.RequiredArgsConstructor;

/**
 * 저장된 뉴스 기사를 조회·가공한다.
 */
@Service
@RequiredArgsConstructor
public class NewsQueryService {

    private static final int MAX_PAGE_SIZE = 20;

    private final NewsArticleRepository newsArticleRepository;

    public NewsHistoryResponse getHistory(final String userEmail, final int page, final int size) {
        final Pageable pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size), Sort.by(Sort.Direction.DESC, "crawledAt"));
        final Page<NewsArticle> historyPage = newsArticleRepository.findByCrawledBy(userEmail, pageable);
        final List<NewsHistoryItemResponse> items = historyPage.getContent().stream()
            .map(this::toHistoryItem)
            .toList();
        return NewsHistoryResponse.builder()
            .page(historyPage.getNumber())
            .size(historyPage.getSize())
            .totalElements(historyPage.getTotalElements())
            .totalPages(historyPage.getTotalPages())
            .items(items)
            .build();
    }

    public NewsArticleDetailResponse getDetail(final String articleId, final String userEmail) {
        final NewsArticle article = newsArticleRepository.findByIdAndCrawledBy(articleId, userEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));
        return toDetail(article);
    }

    private NewsHistoryItemResponse toHistoryItem(final NewsArticle article) {
        return NewsHistoryItemResponse.builder()
            .id(article.getId())
            .sourceOutlet(article.getSourceOutlet())
            .translatedTitle(article.getTranslatedTitle())
            .summary(safeSummary(article))
            .language(article.getLanguage())
            .url(article.getUrl())
            .publishedAt(article.getPublishedAt())
            .crawledAt(article.getCrawledAt())
            .build();
    }

    private NewsArticleDetailResponse toDetail(final NewsArticle article) {
        return NewsArticleDetailResponse.builder()
            .id(article.getId())
            .sourceOutlet(article.getSourceOutlet())
            .url(article.getUrl())
            .language(article.getLanguage())
            .originalTitle(article.getOriginalTitle())
            .translatedTitle(article.getTranslatedTitle())
            .originalContent(article.getOriginalContent())
            .translatedContent(article.getTranslatedContent())
            .summary(safeSummary(article))
            .publishedAt(article.getPublishedAt())
            .crawledAt(article.getCrawledAt())
            .build();
    }

    private List<String> safeSummary(final NewsArticle article) {
        final List<String> summary = article.getSummary();
        if (summary == null || summary.isEmpty()) {
            return List.of();
        }
        return List.copyOf(summary);
    }

    private int sanitizePage(final int page) {
        return Math.max(page, 0);
    }

    private int sanitizeSize(final int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
