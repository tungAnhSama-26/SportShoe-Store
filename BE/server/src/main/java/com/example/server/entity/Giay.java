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
@Table(name = "giay")
public class Giay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 100)
    private String ma;

    @Size(max = 300)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 300)
    private String ten;

    @Column(name = "gioi_tinh")
    private Integer gioiTinh;

    @Size(max = 100)
    @Nationalized
    @Column(name = "chat_lieu", length = 100)
    private String chatLieu;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

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