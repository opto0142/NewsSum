package com.newssum.exception;

/**
 * 뉴스 크롤링 과정에서 발생하는 예외를 표현한다.
 */
public class CrawlingException extends BusinessException {

    public CrawlingException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public CrawlingException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, cause);
    }
}
