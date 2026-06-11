package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tai_khoan_ngan_hang")
@SQLRestriction("deleted = 0")
public class TaiKhoanNganHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_ngan_hang", nullable = false, length = 100)
    private String tenNganHang;

    @Size(max = 50)
    @NotNull
    @Column(name = "so_tai_khoan", nullable = false, length = 50)
    private String soTaiKhoan;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten_chu_tai_khoan", nullable = false, length = 100)
    private String tenChuTaiKhoan;

    @Size(max = 150)
    @Nationalized
    @Column(name = "chi_nhanh", length = 150)
    private String chiNhanh;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "la_mac_dinh", nullable = false)
    private Boolean laMacDinh = false;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao = Instant.now();

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
