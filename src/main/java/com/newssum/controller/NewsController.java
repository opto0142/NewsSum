package com.newssum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newssum.dto.common.ApiResponse;
import com.newssum.dto.news.CrawlNewsRequest;
import com.newssum.dto.news.CrawlNewsResponse;
import com.newssum.dto.news.NewsArticleDetailResponse;
import com.newssum.dto.news.NewsHistoryResponse;
import com.newssum.security.UserPrincipal;
import com.newssum.service.NewsCrawlingService;
import com.newssum.service.NewsQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 프리미엄 사용자의 뉴스 크롤링 요청을 처리한다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsCrawlingService newsCrawlingService;
    private final NewsQueryService newsQueryService;

    @PostMapping("/crawl")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<ApiResponse<CrawlNewsResponse>> crawlNews(
        @Valid @RequestBody final CrawlNewsRequest request,
        @AuthenticationPrincipal final UserPrincipal principal) {

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "뉴스 크롤링을 완료했습니다."));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<ApiResponse<NewsHistoryResponse>> getHistory(
        @AuthenticationPrincipal final UserPrincipal principal,
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "10") final int size) {

        final NewsHistoryResponse response = newsQueryService.getHistory(principal.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<ApiResponse<NewsArticleDetailResponse>> getArticle(
        @PathVariable final String id,
        @AuthenticationPrincipal final UserPrincipal principal) {

        final NewsArticleDetailResponse response = newsQueryService.getDetail(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
