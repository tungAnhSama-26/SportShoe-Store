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
@Table(name = "mau_sac")
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @Size(max = 7)
    @Nationalized
    @Column(name = "ma_mau_hex", length = 7)
    private String maMauHex;

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