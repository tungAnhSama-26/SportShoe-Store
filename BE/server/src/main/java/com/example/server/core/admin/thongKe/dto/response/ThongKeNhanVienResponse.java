package com.example.server.core.admin.thongKe.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ThongKeNhanVienResponse(
        Integer stt,
        UUID nhanVienId,
        String maNhanVien,
        String tenNhanVien,
        String vaiTro,
        Long tongDonHang,
        Long sanPhamDaBan,
        BigDecimal doanhThu
) {
}
