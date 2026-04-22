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
    private Integer giayChiTietId;
    private Integer giayId;
    private String maDotGiamGia;
    private String tenDotGiamGia;
    private String tenGiay; // Base shoe name
    private String mauSac;
    private String kichCo;
    private Integer trangThai;
    private LocalDate ngayTao;
}
