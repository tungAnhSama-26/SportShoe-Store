package com.example.server.core.admin.quanlyhoadon.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record XacNhanThanhToanCodRequest(
        @NotNull(message = "Hinh thuc thanh toan khong duoc de trong")
        Integer hinhThucThanhToan,
        BigDecimal tienKhachDua,
        String ghiChu
) {
}
