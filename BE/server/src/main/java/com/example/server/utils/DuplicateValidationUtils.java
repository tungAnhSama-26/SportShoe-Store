package com.example.server.utils;

import com.example.server.infrastructure.exception.BusinessException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public final class DuplicateValidationUtils {

    private DuplicateValidationUtils() {
    }

    public static void validateUniqueText(String value, boolean exists, String fieldLabel) {
        if (TextUtils.hasText(value) && exists) {
            throw new BusinessException(fieldLabel + " đã tồn tại");
        }
    }

    public static <T> void validateUnique(
            Collection<T> items,
            Function<T, String> extractor,
            String candidate,
            String fieldLabel
    ) {
        String normalizedCandidate = TextUtils.normalizeForComparison(candidate);
        boolean duplicated = items.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .map(TextUtils::normalizeForComparison)
                .anyMatch(normalizedCandidate::equals);

        if (duplicated) {
            throw new BusinessException(fieldLabel + " đã tồn tại");
        }
    }

    public static <T, ID> void validateUniqueForUpdate(
            Collection<T> items,
            Function<T, ID> idExtractor,
            Function<T, String> valueExtractor,
            ID currentId,
            String candidate,
            String fieldLabel
    ) {
        String normalizedCandidate = TextUtils.normalizeForComparison(candidate);
        boolean duplicated = items.stream()
                .filter(item -> !Objects.equals(idExtractor.apply(item), currentId))
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .map(TextUtils::normalizeForComparison)
                .anyMatch(normalizedCandidate::equals);

        if (duplicated) {
            throw new BusinessException(fieldLabel + " đã tồn tại");
        }
    }
}
