package com.example.server.core.admin.quanlyhoadon.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CapNhatTrangThaiHoaDonRequest(
        @NotBlank(message = "Trang thai hoa don khong duoc de trong")
        String trangThai,
        String ghiChu,
        String donViVanChuyen,
        String maVanDon,
        Boolean hoanKho
) {
}
