package com.example.server.infrastructure.exception;

public enum ErrorCode {
    DUPLICATE_DATA("DUPLICATE_DATA", "{0} da ton tai"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "{0} khong ton tai"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Du lieu dau vao khong hop le"),
    INVALID_REQUEST("INVALID_REQUEST", "Yeu cau khong hop le"),
    BUSINESS_ERROR("BUSINESS_ERROR", "Nghiep vu khong hop le"),
    UNEXPECTED_ERROR("UNEXPECTED_ERROR", "Loi he thong khong mong muon");

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
