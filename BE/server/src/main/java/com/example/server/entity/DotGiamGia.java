package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia extends BaseEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "ten", nullable = false, length = 200)
    private String name;

    @Column(name = "mo_ta", length = 500)
    private String description;

    @Column(name = "phan_tram", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "so_tien_giam", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime startAt;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime endAt;

    @Column(name = "kich_hoat", nullable = false)
    private boolean active = true;
}
