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
@Table(name = "phieu_tra_hang_chi_tiet")
public class PhieuTraHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "phieu_tra_hang_id", nullable = false)
    private PhieuTraHang phieuTraHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_chi_tiet_id")
    private HoaDonChiTiet hoaDonChiTiet;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giay_chi_tiet_id", nullable = false)
    private GiayChiTiet giayChiTiet;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "so_luong_tra", nullable = false)
    private Integer soLuongTra;

    @NotNull
    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;

    @NotNull
    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;


}