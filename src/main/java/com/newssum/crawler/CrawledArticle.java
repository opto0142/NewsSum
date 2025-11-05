package com.newssum.crawler;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * Jsoup 크롤러가 추출한 기사 데이터.
 */
@Getter
@Builder
public class CrawledArticle {

    private final String url;
    private final String sourceOutlet;
    private final String title;
    private final String content;
    private final String language;
    private final LocalDateTime publishedAt;
}
