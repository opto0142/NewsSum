package com.newssum.dto.news;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 뉴스 기사 상세 응답.
 */
@Getter
@Builder
@AllArgsConstructor
public class NewsArticleDetailResponse {

    private final String id;
    private final String sourceOutlet;
    private final String url;
    private final String language;
    private final String originalTitle;
    private final String translatedTitle;
    private final String originalContent;
    private final String translatedContent;
    private final List<String> summary;
    private final LocalDateTime publishedAt;
    private final LocalDateTime crawledAt;
}
