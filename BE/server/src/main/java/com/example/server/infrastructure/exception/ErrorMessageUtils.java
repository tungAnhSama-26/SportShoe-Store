package com.example.server.infrastructure.exception;

import java.text.MessageFormat;

public final class ErrorMessageUtils {

    private ErrorMessageUtils() {
    }

    public static String resolve(ErrorCode errorCode, Object... args) {
        return MessageFormat.format(errorCode.messageTemplate(), args);
    }
}
