package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Size(max = 200)
    @Nationalized
    @Column(name = "ma_giao_dich", length = 200)
    private String maGiaoDich;

    @NotNull
    @Column(name = "hinh_thuc", nullable = false)
    private Integer hinhThuc;

    @NotNull
    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "tien_thoi_lai", precision = 18, scale = 2)
    private BigDecimal tienThoiLai;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ngan_hang", length = 100)
    private String nganHang;

    @Size(max = 300)
    @Nationalized
    @Column(name = "noi_dung_ck", length = 300)
    private String noiDungCk;

    @Size(max = 100)
    @Nationalized
    @Column(name = "cong_thanh_toan", length = 100)
    private String congThanhToan;

    @Column(name = "ngay_thanh_toan")
    private Instant ngayThanhToan;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "loai_giao_dich", nullable = false)
    private Integer loaiGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_tra_hang_id")
    private PhieuTraHang phieuTraHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giao_dich_goc_id")
    private ThanhToan giaoDichGoc;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;


}
