package com.example.server.infrastructure.exception;

import java.util.Map;

/** Xung đột tồn khả dụng do sản phẩm vừa được một giao dịch khác giữ/bán trước. */
public class InventoryConflictException extends RuntimeException {

    private final Map<String, String> details;

    public InventoryConflictException(String message, Map<String, String> details) {
        super(message);
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
