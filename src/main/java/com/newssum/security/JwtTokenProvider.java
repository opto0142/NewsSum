package com.newssum.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.newssum.exception.BusinessException;
import com.newssum.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * Generates and validates JWT access/refresh tokens.
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtTokenProvider(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void initSigningKey() {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(final UserPrincipal principal) {
        return buildToken(principal.getUsername(), extractRoles(principal), jwtProperties.getAccessTokenValidity());
    }

    public String generateRefreshToken(final String email) {
        return buildToken(email, null, jwtProperties.getRefreshTokenValidity());
    }

    public String extractEmail(final String token) {
        return parseClaims(token).getSubject();
    }

    public Instant extractExpiration(final String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean validateToken(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, ex);
        }
    }

    public Duration getAccessTokenValidity() {
        return jwtProperties.getAccessTokenValidity();
    }

    public Duration getRefreshTokenValidity() {
        return jwtProperties.getRefreshTokenValidity();
    }

    private String buildToken(final String subject, final Collection<String> roles, final Duration validity) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(validity);

        final Claims claims = Jwts.claims().subject(subject).issuedAt(Date.from(now)).expiration(Date.from(expiry)).build();
        if (roles != null) {
            claims.put("roles", roles);
        }

        return Jwts.builder()
            .claims(claims)
            .signWith(signingKey)
            .compact();
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Collection<String> extractRoles(final UserPrincipal principal) {
        return principal.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .collect(Collectors.toUnmodifiableSet());
    }
}
