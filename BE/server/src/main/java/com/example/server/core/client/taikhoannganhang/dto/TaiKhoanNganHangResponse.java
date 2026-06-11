package com.example.server.core.client.taikhoannganhang.dto;

import java.time.Instant;
import java.util.UUID;

public record TaiKhoanNganHangResponse(
        Integer id,
        UUID khachHangId,
        String tenNganHang,
        String soTaiKhoan,
        String tenChuTaiKhoan,
        String chiNhanh,
        Boolean laMacDinh,
        Instant ngayTao
) {
}
