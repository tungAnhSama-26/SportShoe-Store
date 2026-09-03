package com.example.server.core.admin.quanlyhoadon.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record XacNhanHoanTienRequest(
        @NotNull(message = "Hinh thuc hoan tien khong duoc de trong")
        Integer hinhThucHoanTien,
        BigDecimal soTienHoan,
        String maGiaoDichHoan,
        String ghiChu,
        Integer taiKhoanNganHangId,
        String tenNganHang,
        String soTaiKhoan,
        String tenChuTaiKhoan
) {
    public XacNhanHoanTienRequest(
            Integer hinhThucHoanTien,
            BigDecimal soTienHoan,
            String maGiaoDichHoan,
            String ghiChu,
            Integer taiKhoanNganHangId
    ) {
        this(hinhThucHoanTien, soTienHoan, maGiaoDichHoan, ghiChu, taiKhoanNganHangId, null, null, null);
    }
}
