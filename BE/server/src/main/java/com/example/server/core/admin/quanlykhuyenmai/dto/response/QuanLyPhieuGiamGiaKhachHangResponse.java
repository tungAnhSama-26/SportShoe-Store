package com.example.server.core.admin.quanlykhuyenmai.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QuanLyPhieuGiamGiaKhachHangResponse {
    private Integer id;
    private Integer phieuGiamGiaId;
    private UUID khachHangId;

    private String maPhieuGiamGia;

    private String tenPhieuGiamGia;

    private String tenKhachHang;

    private LocalDate ngaySuDung;

    private Integer trangThai;

    private LocalDate ngayTao;

    public QuanLyPhieuGiamGiaKhachHangResponse(
            Integer id,
            Integer phieuGiamGiaId,
            UUID khachHangId,
            String maPhieuGiamGia,
            String tenPhieuGiamGia,
            String tenKhachHang,
            Instant ngaySuDung,
            Integer trangThai,
            Instant ngayTao
    ) {
        this.id = id;
        this.phieuGiamGiaId = phieuGiamGiaId;
        this.khachHangId = khachHangId;
        this.maPhieuGiamGia = maPhieuGiamGia;
        this.tenPhieuGiamGia = tenPhieuGiamGia;
        this.tenKhachHang = tenKhachHang;
        this.ngaySuDung = toLocalDate(ngaySuDung);
        this.trangThai = trangThai;
        this.ngayTao = toLocalDate(ngayTao);
    }

    private static LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
