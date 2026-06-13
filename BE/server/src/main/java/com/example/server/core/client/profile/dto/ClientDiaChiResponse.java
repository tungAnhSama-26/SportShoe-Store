package com.example.server.core.client.profile.dto;

public record ClientDiaChiResponse(
        Integer id,
        String hoTen,
        String sdt,
        String tinhThanh,
        String quanHuyen,
        String phuongXa,
        String diaChiCuThe,
        Boolean laMacDinh
) {
}
