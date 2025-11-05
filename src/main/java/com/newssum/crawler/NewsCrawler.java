package com.newssum.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.newssum.exception.CrawlingException;
import com.newssum.exception.ErrorCode;

/**
 * 지정한 언론사 URL에서 기사 링크를 추출하고 상세 내용을 읽어온다.
 */
@Component
public class NewsCrawler {

    private static final Logger log = LoggerFactory.getLogger(NewsCrawler.class);
    private static final int TIMEOUT_MS = 10_000;
    private static final String USER_AGENT = "NewsSumBot/1.0 (+https://newssum.example)";

    private final RobotsTxtInspector robotsTxtInspector;

    public NewsCrawler(final RobotsTxtInspector robotsTxtInspector) {
        this.robotsTxtInspector = robotsTxtInspector;
    }

    public List<String> extractArticleLinks(final String sourceUrl, final int limit) {
        robotsTxtInspector.ensureAllowed(sourceUrl);
        try {
            final Document document = Jsoup.connect(sourceUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
            final Elements anchors = document.select("a[href]");
            final Set<String> uniqueLinks = new LinkedHashSet<>();
            for (Element anchor : anchors) {
                final String articleUrl = anchor.absUrl("href");
                if (isArticleCandidate(sourceUrl, articleUrl)) {
                    uniqueLinks.add(articleUrl);
                }
                if (uniqueLinks.size() >= limit) {
                    break;
                }
            }
            return uniqueLinks.stream().limit(limit).collect(Collectors.toList());
        } catch (IOException ex) {
            log.error("기사 링크 추출 실패: url={}", sourceUrl, ex);
            throw new CrawlingException(ErrorCode.CRAWLING_FAILED, ex);
        }
    }

    @Async("crawlerExecutor")
    public CompletableFuture<Optional<CrawledArticle>> fetchArticleAsync(final String articleUrl) {
        try {
            final Document document = Jsoup.connect(articleUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
            final String title = extractTitle(document);
            final String content = extractContent(document);
            final LocalDateTime publishedAt = extractPublishedAt(document);
            final String language = LanguageDetector.detectLanguage(content);
            final CrawledArticle article = CrawledArticle.builder()
                .url(articleUrl)
                .sourceOutlet(extractSourceOutlet(articleUrl, document))
                .title(title)
                .content(content)
                .language(language)
                .publishedAt(publishedAt)
                .build();
            return CompletableFuture.completedFuture(Optional.of(article));
        } catch (HttpStatusException ex) {
            log.warn("기사 접근 실패(HTTP {}): url={}", ex.getStatusCode(), articleUrl);
            return CompletableFuture.completedFuture(Optional.empty());
        } catch (IOException ex) {
            log.warn("기사 크롤링 실패: url={}", articleUrl, ex);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private boolean isArticleCandidate(final String sourceUrl, final String articleUrl) {
        if (articleUrl == null || articleUrl.isBlank()) {
            return false;
        }
        if (!articleUrl.startsWith("http")) {
            return false;
        }
        final String sourceHost = extractHost(sourceUrl);
        final String articleHost = extractHost(articleUrl);
        return sourceHost != null && sourceHost.equals(articleHost) && !articleUrl.equals(sourceUrl);
    }

    private String extractHost(final String url) {
        try {
            final URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException ex) {
            log.debug("호스트 추출 실패: url={}", url, ex);
            return null;
        }
    }

    private String extractTitle(final Document document) {
        final Element metaTitle = document.selectFirst("meta[property=og:title]");
        String title = metaTitle != null ? metaTitle.attr("content") : null;
        if (title == null || title.isBlank()) {
            title = document.title();
        }
        return title;
    }

    private String extractContent(final Document document) {
        final StringBuilder builder = new StringBuilder();
        final Elements paragraphs = document.select("article p");
        if (paragraphs.isEmpty()) {
            document.select("p").stream().limit(50).forEach(paragraph -> builder.append(paragraph.text()).append('\n'));
        } else {
            paragraphs.stream().limit(50).forEach(paragraph -> builder.append(paragraph.text()).append('\n'));
        }
        return builder.toString().trim();
    }

    private LocalDateTime extractPublishedAt(final Document document) {
        final Element metaDate = document.selectFirst("meta[property=article:published_time]");
        if (metaDate != null) {
            final String content = metaDate.attr("content");
            try {
                return OffsetDateTime.parse(content).toLocalDateTime();
            } catch (Exception ignored) {
                // no-op
            }
        }
        final Element timeElement = document.selectFirst("time[datetime]");
        if (timeElement != null) {
            final String datetime = timeElement.attr("datetime");
            try {
                return OffsetDateTime.parse(datetime).toLocalDateTime();
            } catch (Exception ignored) {
                try {
                    return LocalDateTime.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception ignoredAgain) {
                    // no-op
                }
            }
        }
        return null;
    }

    private String extractSourceOutlet(final String articleUrl, final Document document) {
        final Element metaSiteName = document.selectFirst("meta[property=og:site_name]");
        if (metaSiteName != null && !metaSiteName.attr("content").isBlank()) {
            return metaSiteName.attr("content");
        }
        final String host = extractHost(articleUrl);
        return host == null ? "unknown" : host.toLowerCase(Locale.ROOT);
    }
}
