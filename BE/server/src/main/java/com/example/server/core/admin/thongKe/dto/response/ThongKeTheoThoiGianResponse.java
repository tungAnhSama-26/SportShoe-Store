package com.example.server.core.admin.thongKe.dto.response;

import java.math.BigDecimal;

public record ThongKeTheoThoiGianResponse(
        String kyThongKe,
        BigDecimal doanhThu,
        BigDecimal doanhThuThucTe,
        Long soDon,
        BigDecimal giaTriTrungBinh,
        Double tangTruong
) {
}
