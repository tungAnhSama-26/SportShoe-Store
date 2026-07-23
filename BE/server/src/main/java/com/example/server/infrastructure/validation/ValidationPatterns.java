package com.example.server.infrastructure.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String FULL_NAME = "\\p{L}+(?: \\p{L}+)*";
    public static final String VN_PHONE = "^0[35789]\\d{8}$";
    public static final String SHIFT_NAME = "[\\p{L}0-9]+(?: [\\p{L}0-9]+)*";
}
