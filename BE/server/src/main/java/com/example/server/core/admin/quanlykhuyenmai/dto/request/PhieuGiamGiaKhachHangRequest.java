package com.example.server.core.admin.quanlykhuyenmai.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhieuGiamGiaKhachHangRequest {
    private Integer phieuGiamGiaId;

    private String email;

    private LocalDate ngaySuDung;

    private Integer trangThai;

    private LocalDate ngayTao;
}
