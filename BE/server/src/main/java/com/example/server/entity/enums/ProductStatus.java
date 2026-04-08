package com.example.server.entity.enums;

import java.util.Arrays;

public enum ProductStatus {
    DRAFT(1),
    PUBLISHED(2);

    private final int value;

    ProductStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProductStatus fromValue(int value) {
        return Arrays.stream(values())
                .filter(status -> status.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product status value: " + value));
    }
}
