package com.example.server.infrastructure.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        String code,
        String message,
        Map<String, String> errors,
        Instant timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, errorCode.code(), message, null, Instant.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Map<String, String> errors) {
        return new ErrorResponse(false, errorCode.code(), message, errors, Instant.now());
    }
}
