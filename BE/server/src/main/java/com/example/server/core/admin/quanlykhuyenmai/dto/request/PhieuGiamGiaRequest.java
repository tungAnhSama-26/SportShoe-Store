package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhieuGiamGiaRequest {
    private String ma;

    private String ten;

    private Integer loai;

    private BigDecimal giaTri;

    private BigDecimal giaTriToiThieu;

    private BigDecimal giamToiDa;

    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

    private Integer soLuong;

    private Integer soLuongDaDung;

    private Integer trangThai;

    private LocalDate ngayTao;

    private LocalDate ngayCapNhat;
}
