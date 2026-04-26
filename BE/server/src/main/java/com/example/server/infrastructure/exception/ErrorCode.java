package com.example.server.infrastructure.exception;

public enum ErrorCode {
    DUPLICATE_DATA("DUPLICATE_DATA", "{0} đã tồn tại"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "{0} không tồn tại"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu đầu vào không hợp lệ"),
    INVALID_REQUEST("INVALID_REQUEST", "Yêu cầu không hợp lệ"),
    BUSINESS_ERROR("BUSINESS_ERROR", "Nghiệp vụ không hợp lệ"),
    UNEXPECTED_ERROR("UNEXPECTED_ERROR", "Lỗi hệ thống không mong muốn");

    private final String code;
    private final String messageTemplate;

    ErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String code() {
        return code;
    }

    public String messageTemplate() {
        return messageTemplate;
    }
}
