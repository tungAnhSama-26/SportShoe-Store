package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ThanhToanTaiQuayRequest(
        Integer hoaDonId,
        UUID khachHangId,
        String tenKhachHang,
        String soDienThoai,
        String maPhieuGiamGia,
        ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang,
        @NotNull(message = "Hinh thuc thanh toan khong duoc de trong")
        Integer hinhThucThanhToan,
        BigDecimal tienKhachDua,
        String ghiChu,
        List<TaoHoaDonChoItemRequest> items
) {
}
