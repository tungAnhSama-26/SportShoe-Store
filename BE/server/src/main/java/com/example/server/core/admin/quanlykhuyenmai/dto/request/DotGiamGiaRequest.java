package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DotGiamGiaRequest {
    private String ma;

    private String ten;

    private String moTa;

    private Integer loaiGiam;

    private BigDecimal giaTriGiam;

    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

    private Integer kichHoat;

    private LocalDate ngayTao;

    private LocalDate ngayCapNhat;
}
