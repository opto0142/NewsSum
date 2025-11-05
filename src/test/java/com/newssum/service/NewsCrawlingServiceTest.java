package com.newssum.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newssum.crawler.CrawledArticle;
import com.newssum.crawler.NewsCrawler;
import com.newssum.domain.NewsArticle;
import com.newssum.dto.news.CrawlNewsRequest;
import com.newssum.dto.news.CrawlNewsResponse;
import com.newssum.exception.CrawlingException;
import com.newssum.external.gemini.GeminiApiClient;
import com.newssum.external.gemini.GeminiApiClient.SummaryResult;
import com.newssum.external.gemini.GeminiApiClient.TranslationResult;
import com.newssum.repository.NewsArticleRepository;

@ExtendWith(MockitoExtension.class)
class NewsCrawlingServiceTest {

    @Mock
    private NewsCrawler newsCrawler;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private GeminiApiClient geminiApiClient;

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
    void crawlNews_새로운_기사면_AI_처리를_거쳐_DB에_저장한다() {
        final CrawledArticle crawledArticle = CrawledArticle.builder()
            .url("https://example.com/article-1")
            .sourceOutlet("Example")
            .title("Sample Title")
            .content("Sample content")
            .language("en")
            .publishedAt(LocalDateTime.now())
            .build();

        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of(crawledArticle.getUrl()));
        when(newsCrawler.fetchArticleAsync(crawledArticle.getUrl()))
            .thenReturn(CompletableFuture.completedFuture(Optional.of(crawledArticle)));
        when(newsArticleRepository.findByUrlHash(anyString())).thenReturn(Optional.empty());
        when(geminiApiClient.translate(crawledArticle.getTitle(), crawledArticle.getContent()))
            .thenReturn(new TranslationResult("번역 제목", "번역 본문"));
        when(geminiApiClient.summarize("번역 제목", "번역 본문"))
            .thenReturn(new SummaryResult(List.of("요약1", "요약2")));
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(invocation -> {
            final NewsArticle article = invocation.getArgument(0);
            return NewsArticle.builder()
                .id("news-id-1")
                .url(article.getUrl())
                .urlHash(article.getUrlHash())
                .sourceOutlet(article.getSourceOutlet())
                .originalTitle(article.getOriginalTitle())
                .originalContent(article.getOriginalContent())
                .language(article.getLanguage())
                .translatedTitle(article.getTranslatedTitle())
                .translatedContent(article.getTranslatedContent())
                .summary(article.getSummary())
                .publishedAt(article.getPublishedAt())
                .crawledBy(article.getCrawledBy())
                .crawledAt(article.getCrawledAt())
                .build();
        });

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, "user@example.com");

        assertThat(response.getProcessedCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isZero();
        assertThat(response.getArticles()).hasSize(1);
        assertThat(response.getArticles().getFirst().isNewlyCreated()).isTrue();
        assertThat(response.getArticles().getFirst().getId()).isEqualTo("news-id-1");
        final ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(newsArticleRepository).save(captor.capture());
        assertThat(captor.getValue().getTranslatedTitle()).isEqualTo("번역 제목");
        assertThat(captor.getValue().getSummary()).containsExactly("요약1", "요약2");
        verify(geminiApiClient).translate(crawledArticle.getTitle(), crawledArticle.getContent());
        verify(geminiApiClient).summarize("번역 제목", "번역 본문");
        verifyNoMoreInteractions(geminiApiClient);
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
        verify(geminiApiClient, never()).translate(anyString(), anyString());
        verify(geminiApiClient, never()).summarize(anyString(), anyString());
    }

    @Test
    void crawlNews_링크가_없으면_예외를_던진다() {
        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of());

        assertThatThrownBy(() -> newsCrawlingService.crawlNews(request, "user@example.com"))
            .isInstanceOf(CrawlingException.class);
    }

    @Test
    void crawlNews_한국어_기사면_번역없이_요약만_수행한다() {
        final CrawledArticle crawledArticle = CrawledArticle.builder()
            .url("https://example.com/article-3")
            .sourceOutlet("Example")
            .title("한국어 제목")
            .content("한국어 본문")
            .language("ko")
            .publishedAt(LocalDateTime.now())
            .build();

        when(newsCrawler.extractArticleLinks(request.getSourceUrl(), 1)).thenReturn(List.of(crawledArticle.getUrl()));
        when(newsCrawler.fetchArticleAsync(crawledArticle.getUrl()))
            .thenReturn(CompletableFuture.completedFuture(Optional.of(crawledArticle)));
        when(newsArticleRepository.findByUrlHash(anyString())).thenReturn(Optional.empty());
        when(geminiApiClient.summarize("한국어 제목", "한국어 본문"))
            .thenReturn(new SummaryResult(List.of("요약1")));
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(invocation -> {
            final NewsArticle article = invocation.getArgument(0);
            return NewsArticle.builder()
                .id("news-id-2")
                .url(article.getUrl())
                .urlHash(article.getUrlHash())
                .sourceOutlet(article.getSourceOutlet())
                .originalTitle(article.getOriginalTitle())
                .originalContent(article.getOriginalContent())
                .language(article.getLanguage())
                .translatedTitle(article.getTranslatedTitle())
                .translatedContent(article.getTranslatedContent())
                .summary(article.getSummary())
                .publishedAt(article.getPublishedAt())
                .crawledBy(article.getCrawledBy())
                .crawledAt(article.getCrawledAt())
                .build();
        });

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, "user@example.com");

        assertThat(response.getProcessedCount()).isEqualTo(1);
        verify(geminiApiClient, never()).translate(anyString(), anyString());
        verify(geminiApiClient).summarize("한국어 제목", "한국어 본문");
    }
}
