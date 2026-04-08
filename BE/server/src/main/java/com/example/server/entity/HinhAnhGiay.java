package com.example.server.entity;

import com.example.server.entity.enums.ImageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hinh_anh_giay")
public class HinhAnhGiay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giay_chi_tiet_id", nullable = false)
    private GiayChiTiet giayChiTiet;

    @Column(name = "loai_hinh", nullable = false)
    private ImageType imageType = ImageType.PRODUCT;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "mo_ta", length = 300)
    private String description;

    @Column(name = "la_hinh_chinh", nullable = false)
    private boolean primaryImage;
}
