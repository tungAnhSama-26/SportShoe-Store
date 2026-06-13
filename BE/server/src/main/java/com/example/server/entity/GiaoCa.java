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
    @Column(name = "thoi_gian_vao", nullable = false)
    private Instant thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private Instant thoiGianRa;

    @NotNull
    @Column(name = "tien_dau_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienDauCa;

    @NotNull
    @Column(name = "tien_mat_trong_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienMatTrongCa = BigDecimal.ZERO;

    @NotNull
    @Column(name = "tien_chuyen_khoan_trong_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienChuyenKhoanTrongCa = BigDecimal.ZERO;

    @Column(name = "tien_cuoi_ca_thuc_te", precision = 18, scale = 2)
    private BigDecimal tienCuoiCaThucTe;

    @Column(name = "tien_cuoi_ca_he_thong", precision = 18, scale = 2)
    private BigDecimal tienCuoiCaHeThong;

    @Column(name = "tien_chenh_lech", precision = 18, scale = 2)
    private BigDecimal tienChenhLech;

    @Column(name = "ly_do_chenh_lech", length = 255)
    private String lyDoChenhLech;

    @NotNull
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai; // 'MO_CA', 'CHO_BAN_GIAO', 'DA_BAN_GIAO'

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
