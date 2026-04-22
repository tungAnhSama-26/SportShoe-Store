package com.example.server.core.admin.quanlykhuyenmai.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QuanLyPhieuGiamGiaResponse {
    private Integer id;

    private String ma;
    private String ten;
    private Integer loai;
    private Integer loaiPhieu;
    private BigDecimal giaTri;
    private BigDecimal giaTriToiThieu;
    private BigDecimal giamToiDa;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer soLuong;
    private Integer soLuongDaDung;
    private Integer trangThai;
    private LocalDate ngayTao;

    public QuanLyPhieuGiamGiaResponse(
            Integer id,
            String ma,
            String ten,
            Integer loai,
            Integer loaiPhieu,
            BigDecimal giaTri,
            BigDecimal giaTriToiThieu,
            BigDecimal giamToiDa,
            Instant ngayBatDau,
            Instant ngayKetThuc,
            Integer soLuong,
            Integer soLuongDaDung,
            Integer trangThai,
            Instant ngayTao
    ) {
        this.id = id;
        this.ma = ma;
        this.ten = ten;
        this.loai = loai;
        this.loaiPhieu = loaiPhieu == null ? 1 : loaiPhieu;
        this.giaTri = giaTri;
        this.giaTriToiThieu = giaTriToiThieu;
        this.giamToiDa = giamToiDa;
        this.ngayBatDau = toLocalDate(ngayBatDau);
        this.ngayKetThuc = toLocalDate(ngayKetThuc);
        this.soLuong = soLuong;
        this.soLuongDaDung = soLuongDaDung;
        this.trangThai = trangThai;
        this.ngayTao = toLocalDate(ngayTao);
    }

    private static LocalDate toLocalDate(Instant value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
