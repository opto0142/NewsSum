package com.newssum.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Binds JWT configuration properties supplied via {@code application.yml}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Secret key used for signing tokens. */
    private String secret;

    /** Access token validity duration. */
    private Duration accessTokenValidity = Duration.ofHours(1);

    /** Refresh token validity duration. */
    private Duration refreshTokenValidity = Duration.ofDays(7);
}
