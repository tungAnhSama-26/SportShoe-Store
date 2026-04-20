package com.example.server.core.admin.quanlykhuyenmai.dto.response;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QuanLyDotGiamGiaSanPhamResponse {
    private Integer id;
    private Integer dotGiamGiaId;
    private Integer giayId;

    private String tenDotGiamGia;

    private String tenGiay;

    private Integer trangThai;

    private LocalDate ngayTao;
}
