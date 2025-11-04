package com.newssum.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Standardized API response envelope used by all endpoints.
 *
 * @param <T> payload type for successful responses
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String error;

    public static <T> ApiResponse<T> success(final T data, final String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> success(final T data) {
        return success(data, null);
    }

    public static ApiResponse<Void> success(final String message) {
        return new ApiResponse<>(true, null, message, null);
    }

    public static ApiResponse<Void> failure(final String errorCode, final String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
}
