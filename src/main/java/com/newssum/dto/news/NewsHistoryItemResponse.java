package com.newssum.dto.news;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 뉴스 히스토리 목록의 단일 항목을 표현한다.
 */
@Getter
@Builder
@AllArgsConstructor
public class NewsHistoryItemResponse {

    private final String id;
    private final String sourceOutlet;
    private final String translatedTitle;
    private final List<String> summary;
    private final String language;
    private final String url;
    private final LocalDateTime publishedAt;
    private final LocalDateTime crawledAt;
}
