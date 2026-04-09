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
@Table(name = "giay_chi_tiet")
public class GiayChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "giay_id", nullable = false)
    private Giay giay;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "ma_bien_the", nullable = false, length = 150)
    private String maBienThe;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @NotNull
    @Column(name = "gia_goc", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaGoc;

    @NotNull
    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "sku", nullable = false, length = 150)
    private String sku;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "kich_hoat", nullable = false)
    private Integer kichHoat;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;


}