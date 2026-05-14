package com.example.server.core.admin.nhanVien.dto.responsse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class NhanVienResponses {
    
    private NhanVienResponses() {}

    public record NhanVienResponse(
            UUID id,
            String ma,
            String tenDangNhap,
            String hoTen,
            String email,
            String sdt,
            String cccd,
            String gioiTinh,
            LocalDate ngaySinh,
            String diaChi,
            String hinhAnh,
            Integer vaiTro,
            String tenVaiTro,
            Integer trangThai,
            String tenTrangThai,
            Instant ngayTao
    ) {}
}
