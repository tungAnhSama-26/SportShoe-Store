package com.example.server.entity.enums;

import java.util.Arrays;

public enum Gender {
    MALE(1),
    FEMALE(2),
    UNISEX(3);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Gender fromValue(int value) {
        return Arrays.stream(values())
                .filter(gender -> gender.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown gender value: " + value));
    }
}
