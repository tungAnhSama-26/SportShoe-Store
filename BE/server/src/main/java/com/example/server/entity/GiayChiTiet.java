package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "giay_chi_tiet")
public class GiayChiTiet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giay_id", nullable = false)
    private Giay giay;

    @Column(name = "ma_bien_the", nullable = false, unique = true, length = 150)
    private String variantCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mau_sac_id", nullable = false)
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kich_co_id", nullable = false)
    private KichCo kichCo;

    @Column(name = "so_luong", nullable = false)
    private Integer quantity = 0;

    @Column(name = "gia_goc", nullable = false, precision = 18, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "sku", nullable = false, unique = true, length = 150)
    private String sku;

    @Column(name = "kich_hoat", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "variant")
    private List<HinhAnhGiay> hinhAnh = new ArrayList<>();
}
