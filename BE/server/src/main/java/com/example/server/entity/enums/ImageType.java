package com.example.server.entity.enums;

import java.util.Arrays;

public enum ImageType {
    PRODUCT(1),
    THUMBNAIL(2),
    DETAIL(3);

    private final int value;

    ImageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ImageType fromValue(int value) {
        return Arrays.stream(values())
                .filter(type -> type.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown image type value: " + value));
    }
}
