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
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 150)
    private String ma;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "kenh_ban", nullable = false)
    private Integer kenhBan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_giam_gia_id")
    private PhieuGiamGia phieuGiamGia;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_nguoi_nhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Size(max = 20)
    @NotNull
    @Column(name = "sdt_nguoi_nhan", nullable = false, length = 20)
    private String sdtNguoiNhan;

    @Size(max = 300)
    @NotNull
    @Nationalized
    @Column(name = "dia_chi_giao_hang", nullable = false, length = 300)
    private String diaChiGiaoHang;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_lap", nullable = false)
    private Instant ngayLap;

    @Column(name = "ngay_thanh_toan")
    private Instant ngayThanhToan;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @Column(name = "tong_tien_hang", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienHang;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "tien_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienGiam;

    @NotNull
    @Column(name = "tong_tien_thanh_toan", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienThanhToan;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "ghi_chu", length = 1000)
    private String ghiChu;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    /** Hạn giữ hàng tạm khi khách vào thanh toán (null = không giữ). */
    @Column(name = "han_giu_hang")
    private Instant hanGiuHang;

    /** Khách đã xác nhận nhận hàng (cho phép đánh giá). */
    @ColumnDefault("0")
    @Column(name = "da_nhan_hang", nullable = false)
    private Boolean daNhanHang = false;

    /** Đơn đã bị trừ tồn kho chưa (online: chỉ trừ khi nhân viên xác nhận đơn). */
    @ColumnDefault("0")
    @Column(name = "da_tru_kho", nullable = false)
    private Boolean daTruKho = false;


}
