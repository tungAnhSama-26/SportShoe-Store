package com.example.server.core.admin.nhanVien.dto.responsse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GiaoCaResponse(
        UUID id,
        String ma,
        UUID nhanVienTrongCaId,
        String nhanVienTrongCaTen,
        String nhanVienTrongCaMa,
        UUID nhanVienNhanId,
        String nhanVienNhanTen,
        String nhanVienNhanMa,
        Instant thoiGianVao,
        Instant thoiGianRa,
        BigDecimal tienDauCa,
        BigDecimal tienMatTrongCa,
        BigDecimal tienChuyenKhoanTrongCa,
        BigDecimal tienCuoiCaThucTe,
        BigDecimal tienCuoiCaHeThong,
        BigDecimal tienChenhLech,
        String lyDoChenhLech,
        String trangThai,
        String ghiChu
) {}
