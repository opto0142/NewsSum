package com.newssum.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newssum.crawler.CrawledArticle;
import com.newssum.crawler.NewsCrawler;
import com.newssum.domain.NewsArticle;
import com.newssum.dto.news.CrawlNewsRequest;
import com.newssum.dto.news.CrawlNewsResponse;
import com.newssum.exception.CrawlingException;
import com.newssum.repository.NewsArticleRepository;

@ExtendWith(MockitoExtension.class)
class NewsCrawlingServiceTest {

    @Mock
    private NewsCrawler newsCrawler;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @InjectMocks
    private NewsCrawlingService newsCrawlingService;

    private CrawlNewsRequest request;

    @BeforeEach
    void setUp() {
        request = CrawlNewsRequest.builder()
            .sourceUrl("https://example.com")
            .articleCount(1)
            .build();
    }

    @Test
    void crawlNews_새로운_기사면_DB에_저장하고_응답에_포함한다() {
        final CrawledArticle crawledArticle = CrawledArticle.builder()
            .url("https://example.com/article-1")
            .sourceOutlet("Example")
            .title("Sample Title")
            .content("Sample content")
            .language("en")
            .publishedAt(LocalDateTime.now())
            .build();
        final NewsArticle stored = NewsArticle.builder()
            .id("news-id-1")
            .url(crawledArticle.getUrl())
            .urlHash("hash")
            .sourceOutlet(crawledArticle.getSourceOutlet())
            .originalTitle(crawledArticle.getTitle())
            .originalContent(crawledArticle.getContent())
            .language(crawledArticle.getLanguage())
            .publishedAt(crawledArticle.getPublishedAt())
            .crawledBy("user@example.com")
            .crawledAt(LocalDateTime.now())
            .build();

        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of(crawledArticle.getUrl()));
        when(newsCrawler.fetchArticleAsync(crawledArticle.getUrl()))
            .thenReturn(CompletableFuture.completedFuture(Optional.of(crawledArticle)));
        when(newsArticleRepository.findByUrlHash(anyString())).thenReturn(Optional.empty());
        when(newsArticleRepository.save(any(NewsArticle.class))).thenReturn(stored);

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, "user@example.com");

        assertThat(response.getProcessedCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isZero();
        assertThat(response.getArticles()).hasSize(1);
        assertThat(response.getArticles().getFirst().isNewlyCreated()).isTrue();
        assertThat(response.getArticles().getFirst().getId()).isEqualTo("news-id-1");
        verify(newsArticleRepository, times(1)).save(any(NewsArticle.class));
    }

    @Test
    void crawlNews_이미_존재하는_URL이면_저장하지_않고_건너뛴다() {
        final CrawledArticle crawledArticle = CrawledArticle.builder()
            .url("https://example.com/article-2")
            .sourceOutlet("Example")
            .title("Existing Title")
            .content("Existing content")
            .language("en")
            .publishedAt(LocalDateTime.now())
            .build();
        final NewsArticle existing = NewsArticle.builder()
            .id("existing-id")
            .url(crawledArticle.getUrl())
            .urlHash("hash")
            .sourceOutlet(crawledArticle.getSourceOutlet())
            .originalTitle(crawledArticle.getTitle())
            .originalContent(crawledArticle.getContent())
            .language(crawledArticle.getLanguage())
            .publishedAt(crawledArticle.getPublishedAt())
            .crawledBy("user@example.com")
            .crawledAt(LocalDateTime.now())
            .build();

        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of(crawledArticle.getUrl()));
        when(newsCrawler.fetchArticleAsync(crawledArticle.getUrl()))
            .thenReturn(CompletableFuture.completedFuture(Optional.of(crawledArticle)));
        when(newsArticleRepository.findByUrlHash(anyString())).thenReturn(Optional.of(existing));

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, "user@example.com");

        assertThat(response.getProcessedCount()).isZero();
        assertThat(response.getSkippedCount()).isEqualTo(1);
        assertThat(response.getArticles()).hasSize(1);
        assertThat(response.getArticles().getFirst().isNewlyCreated()).isFalse();
        verify(newsArticleRepository, times(0)).save(any(NewsArticle.class));
    }

    @Test
    void crawlNews_링크가_없으면_예외를_던진다() {
        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of());

        assertThatThrownBy(() -> newsCrawlingService.crawlNews(request, "user@example.com"))
            .isInstanceOf(CrawlingException.class);
    }
}
