package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DotGiamGiaSanPhamRequest {
    private Integer dotGiamGiaId;

    private Integer giayChiTietId;

    private Integer trangThai;

    private LocalDate ngayTao;
}
