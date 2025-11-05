package com.newssum.dto.news;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 뉴스 크롤링 요청 페이로드.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlNewsRequest {

    private static final String URL_PATTERN = "https?://.+";

    @NotBlank(message = "크롤링할 URL을 입력해주세요.")
    @Pattern(regexp = URL_PATTERN, message = "http(s)로 시작하는 올바른 URL을 입력해주세요.")
    private String sourceUrl;

    @Min(value = 1, message = "최소 1개의 기사를 요청해야 합니다.")
    @Max(value = 20, message = "최대 20개까지 요청할 수 있습니다.")
    private Integer articleCount;

    public int resolveArticleCount(final int defaultCount) {
        return articleCount == null ? defaultCount : articleCount;
    }
}
