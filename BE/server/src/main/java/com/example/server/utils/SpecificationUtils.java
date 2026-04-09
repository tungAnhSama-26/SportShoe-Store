package com.example.server.utils;

public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static boolean hasText(String value) {
        return TextUtils.hasText(value);
    }

    public static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
