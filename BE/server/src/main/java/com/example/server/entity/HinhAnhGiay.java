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

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "hinh_anh_giay")
public class HinhAnhGiay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "giay_chi_tiet_id", nullable = false)
    private GiayChiTiet giayChiTiet;

    @NotNull
    @Column(name = "loai_hinh", nullable = false)
    private Integer loaiHinh;

    @Size(max = 1000)
    @NotNull
    @Nationalized
    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Size(max = 300)
    @Nationalized
    @Column(name = "mo_ta", length = 300)
    private String moTa;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "la_hinh_chinh", nullable = false)
    private Boolean laHinhChinh;

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