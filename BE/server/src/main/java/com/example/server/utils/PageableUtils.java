package com.example.server.utils;

import com.example.server.infrastructure.AppProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtils {

    private PageableUtils() {
    }

    public static Pageable resolvePageable(
            Integer page,
            Integer size,
            String sortBy,
            Sort.Direction direction,
            AppProperties appProperties
    ) {
        int resolvedPage = page == null || page < 0 ? appProperties.pagination().defaultPage() : page;
        int resolvedSize = size == null || size <= 0
                ? appProperties.pagination().defaultSize()
                : Math.min(size, appProperties.pagination().maxSize());

        if (!TextUtils.hasText(sortBy)) {
            return PageRequest.of(resolvedPage, resolvedSize);
        }

        Sort.Direction resolvedDirection = direction == null ? Sort.Direction.ASC : direction;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(resolvedDirection, sortBy));
    }
}
