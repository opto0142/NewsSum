package com.newssum.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.newssum.external.gemini.GeminiProperties;

/**
 * Gemini API 호출용 RestTemplate 을 구성한다.
 */
@Configuration
public class GeminiClientConfig {

    @Bean
    public RestTemplate geminiRestTemplate(final RestTemplateBuilder builder, final GeminiProperties properties) {
        final Duration timeout = properties.getTimeout();
        return builder
            .rootUri(properties.getBaseUrl())
            .setConnectTimeout(timeout)
            .setReadTimeout(timeout)
            .build();
    }
}
