package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TaoHoaDonChoItemRequest(
        @NotNull(message = "Chi tiet san pham khong duoc de trong")
        Integer chiTietId,
        @NotNull(message = "So luong khong duoc de trong")
        @Min(value = 1, message = "So luong phai lon hon 0")
        Integer soLuong
) {
}
