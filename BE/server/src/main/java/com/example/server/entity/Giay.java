package com.example.server.entity;

import com.example.server.entity.enums.Gender;
import com.example.server.entity.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "giay")
public class Giay extends BaseEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "ten", nullable = false, length = 300)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thuong_hieu_id", nullable = false)
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loai_giay_id")
    private LoaiGiay loaiGiay;

    @Column(name = "gioi_tinh")
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_lieu_id")
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dot_giam_gia_id")
    private DotGiamGia dotGiamGia;

    @Column(name = "mo_ta", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "trang_thai", nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @OneToMany(mappedBy = "product")
    private List<GiayChiTiet> bienThe = new ArrayList<>();
}
