package com.example.server.core.admin.nhanVien.dto.responsse;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
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
            String gioiTinh,
            LocalDate ngaySinh,
            DiaChiHaiCapResponse diaChi,
            String hinhAnh,
            Integer vaiTro,
            String tenVaiTro,
            Integer trangThai,
            String tenTrangThai,
            Instant ngayTao,
            String matKhauTamThoi,
            Boolean emailDaGuiThanhCong,
            String canhBaoEmail,
            Boolean batBuocDoiMatKhau,
            Instant hanDoiMatKhau,
            String faceDescriptor
    ) {}
}
