package com.newssum.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newssum.dto.common.ApiResponse;
import com.newssum.dto.news.CrawlNewsRequest;
import com.newssum.dto.news.CrawlNewsResponse;
import com.newssum.security.UserPrincipal;
import com.newssum.service.NewsCrawlingService;

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

    @PostMapping("/crawl")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<ApiResponse<CrawlNewsResponse>> crawlNews(
        @Valid @RequestBody final CrawlNewsRequest request,
        @AuthenticationPrincipal final UserPrincipal principal) {

        final CrawlNewsResponse response = newsCrawlingService.crawlNews(request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "뉴스 크롤링을 완료했습니다."));
    }
}
