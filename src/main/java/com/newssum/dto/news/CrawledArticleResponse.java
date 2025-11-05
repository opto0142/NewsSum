package com.newssum.dto.news;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 크롤링된 개별 기사 응답.
 */
@Getter
@Builder
@AllArgsConstructor
public class CrawledArticleResponse {

    private final String id;
    private final String url;
    private final String sourceOutlet;
    private final String originalTitle;
    private final String language;
    private final LocalDateTime publishedAt;
    private final boolean newlyCreated;
}
