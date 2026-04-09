package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 100)
    private String ma;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @NotNull
    @Column(name = "loai", nullable = false)
    private Integer loai;

    @NotNull
    @Column(name = "gia_tri", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "gia_tri_toi_thieu", precision = 18, scale = 2)
    private BigDecimal giaTriToiThieu;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "ngay_bat_dau")
    private Instant ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Instant ngayKetThuc;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "so_luong_da_dung", nullable = false)
    private Integer soLuongDaDung;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;


}