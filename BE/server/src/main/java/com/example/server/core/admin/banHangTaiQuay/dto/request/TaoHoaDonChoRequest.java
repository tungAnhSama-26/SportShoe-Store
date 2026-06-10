package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record TaoHoaDonChoRequest(
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        String maPhieuGiamGia,
        ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang,
        List<@Valid TaoHoaDonChoItemRequest> items
) {
}
