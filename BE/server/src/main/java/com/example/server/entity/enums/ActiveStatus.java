package com.example.server.entity.enums;

import java.util.Arrays;

public enum ActiveStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    ActiveStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ActiveStatus fromValue(int value) {
        return Arrays.stream(values())
                .filter(status -> status.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown active status value: " + value));
    }
}
