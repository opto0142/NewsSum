package com.newssum.exception;

import org.springframework.http.HttpStatus;

/**
 * Enumerates application-wide error codes and default messages.
 */
public enum ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_PREMIUM(HttpStatus.CONFLICT, "이미 프리미엄 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    PROMO_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 프로모션 코드입니다."),
    PROMO_CODE_INACTIVE(HttpStatus.BAD_REQUEST, "이미 사용된 프로모션 코드입니다."),
    PROMO_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 프로모션 코드입니다."),
    PROMO_CODE_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 프로모션 코드입니다."),
    INVALID_URL(HttpStatus.BAD_REQUEST, "올바르지 않은 URL입니다."),
    CRAWLING_DISALLOWED(HttpStatus.FORBIDDEN, "사이트에서 크롤링이 허용되지 않습니다."),
    CRAWLING_FAILED(HttpStatus.BAD_GATEWAY, "뉴스를 가져올 수 없습니다."),
    CRAWLING_NO_ARTICLE(HttpStatus.NOT_FOUND, "수집할 기사를 찾지 못했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
