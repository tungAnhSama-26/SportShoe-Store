package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "giao_ca")
public class GiaoCa {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_trong_ca_id", nullable = false)
    private NhanVien nhanVienTrongCa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_nhan_id")
    private NhanVien nhanVienNhan;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ca_lam_id", nullable = false)
    private CaLam caLam;

    @NotNull
    @Column(name = "thoi_gian_vao", nullable = false)
    private Instant thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private Instant thoiGianRa;

    @Column(name = "thoi_gian_xac_nhan")
    private Instant thoiGianXacNhan;

    @NotNull
    @Column(name = "tien_dau_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienDauCa;

    @Column(name = "tien_cuoi_ca_thuc_te", precision = 18, scale = 2)
    private BigDecimal tienCuoiCaThucTe;

    @Column(name = "tien_nhan_kiem_dem", precision = 18, scale = 2)
    private BigDecimal tienNhanKiemDem;

    @Column(name = "ly_do_chenh_lech", length = 255)
    private String lyDoChenhLech;

    @NotNull
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ca_chua_ket_thuc")
    private Integer caChuaKetThuc;
}
