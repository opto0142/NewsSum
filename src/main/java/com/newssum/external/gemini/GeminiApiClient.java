package com.newssum.external.gemini;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.newssum.exception.GeminiApiException;
import com.newssum.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * Gemini API를 호출하여 번역 및 요약 결과를 제공한다.
 */
@Component
@RequiredArgsConstructor
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    private final GeminiProperties properties;
    private final RestTemplate geminiRestTemplate;

    public TranslationResult translate(final String title, final String content) {
        final String prompt = GeminiPromptFactory.translationPrompt(title, content);
        final GeminiRequest request = createRequest(prompt, properties.getMaxOutputTokens());
        return executeWithRetry(() -> parseTranslation(sendRequest(request)));
    }

    public SummaryResult summarize(final String title, final String content) {
        final String prompt = GeminiPromptFactory.summaryPrompt(title, content);
        final GeminiRequest request = createRequest(prompt, Math.min(600, properties.getMaxOutputTokens()));
        return executeWithRetry(() -> parseSummary(sendRequest(request)));
    }

    private GeminiResponse sendRequest(final GeminiRequest request) {
        final String apiKey = requireApiKey();
        final String path = UriComponentsBuilder.fromPath("/models/%s:generateContent".formatted(properties.getModel()))
            .queryParam("key", apiKey)
            .toUriString();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        try {
            return geminiRestTemplate.postForObject(path, new HttpEntity<>(request, headers), GeminiResponse.class);
        } catch (RestClientException ex) {
            log.warn("Gemini API 호출 실패", ex);
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE, ex);
        }
    }

    private TranslationResult parseTranslation(final GeminiResponse response) {
        final String text = extractFirstText(response);
        if (text == null) {
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE);
        }
        final String[] parts = text.split("\n", 2);
        final String translatedTitle = parts.length > 0 ? normalize(parts[0].replace("Title:", "")) : "";
        final String translatedContent = parts.length > 1 ? normalize(parts[1].replace("Content:", "")) : "";
        return new TranslationResult(translatedTitle, translatedContent);
    }

    private SummaryResult parseSummary(final GeminiResponse response) {
        final String text = extractFirstText(response);
        if (text == null) {
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE);
        }
        final List<String> items = text.lines()
            .map(line -> line.replaceFirst("^[\\-•\\*]+\\s?", ""))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .limit(5)
            .toList();
        if (items.isEmpty()) {
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE);
        }
        return new SummaryResult(items);
    }

    private String extractFirstText(final GeminiResponse response) {
        if (response == null || CollectionUtils.isEmpty(response.candidates())) {
            return null;
        }
        for (GeminiCandidate candidate : response.candidates()) {
            if (candidate.content() == null || CollectionUtils.isEmpty(candidate.content().parts())) {
                continue;
            }
            for (GeminiPart part : candidate.content().parts()) {
                if (part.text() != null && !part.text().isBlank()) {
                    return part.text();
                }
            }
        }
        return null;
    }

    private GeminiRequest createRequest(final String prompt, final int maxOutputTokens) {
        return new GeminiRequest(
            List.of(new GeminiContent(List.of(new GeminiPart(prompt)))),
            new GeminiGenerationConfig(properties.getTemperature(), maxOutputTokens)
        );
    }

    private <T> T executeWithRetry(final Supplier<T> supplier) {
        final int maxAttempts = Math.max(properties.getMaxRetries(), 1);
        int attempt = 0;
        while (true) {
            try {
                return supplier.get();
            } catch (GeminiApiException ex) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw ex;
                }
                final long delayMillis = (long) Math.pow(2, attempt - 1) * 1_000L;
                sleep(delayMillis + ThreadLocalRandom.current().nextLong(250));
            }
        }
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE, ex);
        }
    }

    private String normalize(final String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }

    private String requireApiKey() {
        final String apiKey = properties.getApi().getKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiApiException(ErrorCode.GEMINI_API_FAILURE);
        }
        return apiKey;
    }

    public record TranslationResult(String translatedTitle, String translatedContent) { }

    public record SummaryResult(List<String> summary) { }

    public record GeminiRequest(List<GeminiContent> contents, GeminiGenerationConfig generationConfig) { }

    public record GeminiContent(List<GeminiPart> parts) { }

    public record GeminiPart(String text) { }

    public record GeminiGenerationConfig(float temperature, int maxOutputTokens) { }

    public record GeminiResponse(List<GeminiCandidate> candidates) { }

    public record GeminiCandidate(GeminiContent content) { }
}
