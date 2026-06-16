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
@Table(name = "phieu_tra_hang")
public class PhieuTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 150)
    private String ma;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ly_do", length = 500)
    private String lyDo;

    @NotNull
    @Column(name = "tong_tien_hoan", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienHoan;

    @NotNull
    @Column(name = "hinh_thuc_hoan", nullable = false)
    private Integer hinhThucHoan;

    @NotNull
    @ColumnDefault("2")
    @Column(name = "loai_yeu_cau", nullable = false)
    private Integer loaiYeuCau;

    @Size(max = 50)
    @Nationalized
    @Column(name = "ly_do_ma", length = 50)
    private String lyDoMa;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "mo_ta", length = 1000)
    private String moTa;

    @NotNull
    @Column(name = "tong_tien_du_kien", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienDuKien;

    @NotNull
    @Column(name = "tong_tien_thuc_te", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienThucTe;

    @Size(max = 100)
    @Nationalized
    @Column(name = "don_vi_van_chuyen", length = 100)
    private String donViVanChuyen;

    @Size(max = 150)
    @Nationalized
    @Column(name = "ma_van_don_hoan", length = 150)
    private String maVanDonHoan;

    @Column(name = "trang_thai_van_chuyen")
    private Integer trangThaiVanChuyen;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ly_do_tu_choi", length = 500)
    private String lyDoTuChoi;

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

    @Column(name = "ngay_duyet")
    private Instant ngayDuyet;

    @Column(name = "ngay_gui_hang")
    private Instant ngayGuiHang;

    @Column(name = "ngay_nhan_hang")
    private Instant ngayNhanHang;

    @Column(name = "ngay_kiem_tra")
    private Instant ngayKiemTra;

    @Column(name = "ngay_hoan_tat")
    private Instant ngayHoanTat;

}
