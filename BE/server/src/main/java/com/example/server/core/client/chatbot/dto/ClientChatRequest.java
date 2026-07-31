package com.example.server.core.client.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientChatRequest(
        Integer sessionId,

        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        @Size(max = 2000, message = "Tin nhắn không được vượt quá 2000 ký tự")
        String message,

        @Size(max = 100, message = "Tên khách hàng không vượt quá 100 ký tự")
        String customerName,

        @Pattern(regexp = "^$|^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
        String phoneNumber
) {
}
