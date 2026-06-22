package com.example.server.core.admin.nhanVien.dto.responsse;

public record CaLamResponse(
        String id,
        String ten,
        String gioBatDau,
        String gioKetThuc,
        Boolean trangThai
) {}
