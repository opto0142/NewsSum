package com.newssum.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.newssum.domain.NewsArticle;
import com.newssum.dto.news.NewsArticleDetailResponse;
import com.newssum.dto.news.NewsHistoryResponse;
import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;
import com.newssum.repository.NewsArticleRepository;

@ExtendWith(MockitoExtension.class)
class NewsQueryServiceTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @InjectMocks
    private NewsQueryService newsQueryService;

    @Test
    void getHistory_요청한_페이지와_크기가_정상화되어_기사목록을_반환한다() {
        final String userEmail = "user@example.com";
        final NewsArticle article = NewsArticle.builder()
            .id("article-1")
            .url("https://example.com/news/1")
            .sourceOutlet("Example Outlet")
            .translatedTitle("번역된 제목")
            .summary(List.of("요약1", "요약2"))
            .language("en")
            .publishedAt(LocalDateTime.parse("2025-11-01T10:15:30"))
            .crawledAt(LocalDateTime.parse("2025-11-02T09:00:00"))
            .build();

        when(newsArticleRepository.findByCrawledBy(anyString(), any(Pageable.class)))
            .thenAnswer(invocation -> {
                final Pageable pageable = invocation.getArgument(1);
                return new PageImpl<>(List.of(article), pageable, 87);
            });

        final NewsHistoryResponse response = newsQueryService.getHistory(userEmail, 2, 50);

        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(newsArticleRepository).findByCrawledBy(eq(userEmail), pageableCaptor.capture());
        final Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("crawledAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("crawledAt").getDirection()).isEqualTo(Sort.Direction.DESC);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getId()).isEqualTo("article-1");
        assertThat(response.getItems().getFirst().getSummary()).containsExactly("요약1", "요약2");
    assertThat(response.getTotalElements()).isEqualTo(87);
        assertThat(response.getSize()).isEqualTo(20);
    }

    @Test
    void getDetail_본인기사면_상세정보를_반환한다() {
        final String userEmail = "user@example.com";
        final NewsArticle article = NewsArticle.builder()
            .id("article-42")
            .url("https://example.com/news/42")
            .sourceOutlet("Example Outlet")
            .originalTitle("Original Title")
            .translatedTitle("Translated Title")
            .originalContent("Original content")
            .translatedContent("Translated content")
            .summary(List.of("포인트"))
            .language("en")
            .publishedAt(LocalDateTime.parse("2025-10-31T08:00:00"))
            .crawledAt(LocalDateTime.parse("2025-11-01T08:00:00"))
            .build();

        when(newsArticleRepository.findByIdAndCrawledBy("article-42", userEmail)).thenReturn(Optional.of(article));

        final NewsArticleDetailResponse detail = newsQueryService.getDetail("article-42", userEmail);

        assertThat(detail.getId()).isEqualTo("article-42");
        assertThat(detail.getTranslatedTitle()).isEqualTo("Translated Title");
        assertThat(detail.getSummary()).containsExactly("포인트");
        assertThat(detail.getOriginalContent()).isEqualTo("Original content");
    }

    @Test
    void getDetail_존재하지_않으면_BusinessException을_던진다() {
        when(newsArticleRepository.findByIdAndCrawledBy("missing", "user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsQueryService.getDetail("missing", "user@example.com"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NEWS_NOT_FOUND);
    }
}
