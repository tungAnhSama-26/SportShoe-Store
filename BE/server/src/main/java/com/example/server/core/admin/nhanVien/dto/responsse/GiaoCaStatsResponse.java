package com.example.server.core.admin.nhanVien.dto.responsse;

import java.math.BigDecimal;

public record GiaoCaStatsResponse(
        BigDecimal tienMatTrongCa,
        BigDecimal tienChuyenKhoanTrongCa,
        BigDecimal tienCuoiCaHeThong
) {}
