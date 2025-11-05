package com.newssum.dto.news;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자별 뉴스 히스토리 응답.
 */
@Getter
@Builder
@AllArgsConstructor
public class NewsHistoryResponse {

    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final List<NewsHistoryItemResponse> items;
}
