package com.newssum.exception;

/**
 * Gemini API 연동 중 발생하는 예외를 표현한다.
 */
public class GeminiApiException extends BusinessException {

    public GeminiApiException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public GeminiApiException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, cause);
    }
}
