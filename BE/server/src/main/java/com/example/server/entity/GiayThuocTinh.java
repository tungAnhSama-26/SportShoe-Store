package com.example.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "giay_thuoc_tinh")
public class GiayThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "giay_id", nullable = false)
    private Giay giay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_giay_id")
    private DeGiay deGiay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_giay_id")
    private CoGiay coGiay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trong_luong_id")
    private TrongLuong trongLuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cong_nghe_dem_id")
    private CongNgheDem congNgheDem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_lieu_giay_id")
    private ChatLieuGiay chatLieuGiay;

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
