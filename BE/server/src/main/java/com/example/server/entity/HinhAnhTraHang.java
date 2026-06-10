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
@Table(name = "hinh_anh_tra_hang")
public class HinhAnhTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phieu_tra_hang_id", nullable = false)
    private PhieuTraHang phieuTraHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_tra_hang_chi_tiet_id")
    private PhieuTraHangChiTiet phieuTraHangChiTiet;

    @Size(max = 1000)
    @NotNull
    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @NotNull
    @Column(name = "loai_anh", nullable = false)
    private Integer loaiAnh;

    @Size(max = 500)
    @Nationalized
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @NotNull
    @ColumnDefault("sysdatetimeoffset()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;
}
