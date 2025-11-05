package com.newssum.external.gemini;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;
import lombok.Setter;

/**
 * Gemini API 호출에 필요한 설정 값을 보관한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    private final Api api = new Api();
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String model = "gemini-pro";
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private float temperature = 0.3f;
    private int maxOutputTokens = 1000;

    @Getter
    @Setter
    public static class Api {

        private String key;
    }
}
