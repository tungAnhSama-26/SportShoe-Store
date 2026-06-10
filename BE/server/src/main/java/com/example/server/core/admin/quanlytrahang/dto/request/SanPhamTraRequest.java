package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SanPhamTraRequest(
        @NotNull(message = "Vui lòng chọn sản phẩm cần trả")
        Integer hoaDonChiTietId,

        @NotNull(message = "Vui lòng nhập số lượng cần trả")
        @Min(value = 1, message = "Số lượng cần trả phải lớn hơn 0")
        Integer soLuong,

        @Size(max = 500, message = "Ghi chú sản phẩm không được vượt quá 500 ký tự")
        String ghiChu
) {
}
