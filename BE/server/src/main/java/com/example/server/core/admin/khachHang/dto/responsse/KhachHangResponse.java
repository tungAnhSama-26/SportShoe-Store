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
        String hinhAnh,
        Integer trangThai,
        String tenTrangThai,
        Instant ngayTao
) {}
