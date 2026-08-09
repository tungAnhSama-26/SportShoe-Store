package com.example.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Embeddable
public class DiaChiHaiCap {

    @Size(max = 100)
    @Nationalized
    @Column(name = "tinh_thanh", length = 100)
    private String tinhThanh;

    @Size(max = 100)
    @Nationalized
    @Column(name = "phuong_xa", length = 100)
    private String phuongXa;

    @Size(max = 300)
    @Nationalized
    @Column(name = "dia_chi_cu_the", length = 300)
    private String diaChiCuThe;
}
