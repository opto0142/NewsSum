package com.newssum.dto.news;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 뉴스 크롤링 결과 응답.
 */
@Getter
@Builder
@AllArgsConstructor
public class CrawlNewsResponse {

    private final int requestedCount;
    private final int processedCount;
    private final int skippedCount;
    private final List<CrawledArticleResponse> articles;
}
