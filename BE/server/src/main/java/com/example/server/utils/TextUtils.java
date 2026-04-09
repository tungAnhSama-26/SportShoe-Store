package com.example.server.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String normalizeForComparison(String value) {
        if (!hasText(value)) {
            return "";
        }

        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
