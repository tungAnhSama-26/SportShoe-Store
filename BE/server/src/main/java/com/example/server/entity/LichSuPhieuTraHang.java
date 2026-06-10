package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lich_su_phieu_tra_hang")
public class LichSuPhieuTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phieu_tra_hang_id", nullable = false)
    private PhieuTraHang phieuTraHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Column(name = "trang_thai_cu")
    private Integer trangThaiCu;

    @NotNull
    @Column(name = "trang_thai_moi", nullable = false)
    private Integer trangThaiMoi;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "hanh_dong", nullable = false, length = 150)
    private String hanhDong;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "ghi_chu", length = 1000)
    private String ghiChu;

    @NotNull
    @ColumnDefault("sysdatetimeoffset()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;
}
