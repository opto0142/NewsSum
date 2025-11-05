package com.newssum.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.newssum.exception.CrawlingException;
import com.newssum.exception.ErrorCode;

/**
 * robots.txt 규칙을 확인하여 크롤링 허용 여부를 검증한다.
 */
@Component
public class RobotsTxtInspector {

    private static final Logger log = LoggerFactory.getLogger(RobotsTxtInspector.class);
    private static final int TIMEOUT_MS = 10_000;
    private static final String USER_AGENT = "NewsSumBot/1.0 (+https://newssum.example)";

    public void ensureAllowed(final String targetUrl) {
        try {
            final URI uri = URI.create(targetUrl);
            final URL url = uri.toURL();
            final String robotsUrl = "%s://%s/robots.txt".formatted(url.getProtocol(), url.getHost());
            final Connection.Response response = Jsoup.connect(robotsUrl)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .execute();
            final String body = response.body().toLowerCase(Locale.ROOT);
            if (body.contains("disallow: /")) {
                throw new CrawlingException(ErrorCode.CRAWLING_DISALLOWED);
            }
        } catch (IllegalArgumentException ex) {
            throw new CrawlingException(ErrorCode.INVALID_URL, ex);
        } catch (MalformedURLException ex) {
            throw new CrawlingException(ErrorCode.INVALID_URL, ex);
        } catch (IOException ex) {
            log.warn("robots.txt 확인 실패: url={}", targetUrl, ex);
        }
    }
}
