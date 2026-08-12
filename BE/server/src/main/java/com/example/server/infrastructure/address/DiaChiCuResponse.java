package com.example.server.infrastructure.address;

public record DiaChiCuResponse(
        boolean daAnhXa,
        String tinhThanhCode,
        String tinhThanh,
        String phuongXaCode,
        String phuongXa,
        String diaChiCuThe,
        String thongBao
) {
}
