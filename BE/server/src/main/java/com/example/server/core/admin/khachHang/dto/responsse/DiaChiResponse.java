package com.example.server.core.admin.khachHang.dto.responsse;

public record DiaChiResponse(
        Integer id,
        String hoTen,
        String sdt,
        String tinhThanh,
        String quanHuyen,
        String phuongXa,
        String diaChiCuThe,
        Boolean laMacDinh
) {}
