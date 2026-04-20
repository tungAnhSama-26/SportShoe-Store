package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record TaoHoaDonChoRequest(
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        String maPhieuGiamGia,
        @NotEmpty(message = "Danh sach san pham khong duoc de trong")
        List<TaoHoaDonChoItemRequest> items
) {
}
