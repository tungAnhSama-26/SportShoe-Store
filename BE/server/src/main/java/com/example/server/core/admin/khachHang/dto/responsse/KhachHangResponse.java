package com.example.server.core.admin.khachHang.dto.responsse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record KhachHangResponse(
        UUID id,
        String tenDangNhap,
        String hoTen,
        String email,
        String sdt,
        LocalDate ngaySinh,
        Integer gioiTinh,
        String tenGioiTinh,
        String hinhAnh,
        Integer trangThai,
        String tenTrangThai,
        Instant ngayTao,
        String diaChiMacDinh,
        String sdtMacDinh,
        Boolean emailDaGuiThanhCong,
        String canhBaoEmail
) {}
