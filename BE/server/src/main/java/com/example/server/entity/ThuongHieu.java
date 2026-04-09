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
@Table(name = "thuong_hieu")
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @Size(max = 100)
    @Nationalized
    @Column(name = "xuat_xu", length = 100)
    private String xuatXu;

    @Size(max = 500)
    @Nationalized
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Size(max = 500)
    @Nationalized
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Size(max = 300)
    @Nationalized
    @Column(name = "website", length = 300)
    private String website;

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