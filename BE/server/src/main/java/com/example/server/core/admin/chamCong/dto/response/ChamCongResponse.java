package com.example.server.core.admin.chamCong.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ChamCongResponse(
    UUID id,
    UUID nhanVienId,
    String tenNhanVien,
    LocalDate ngay,
    String ca,
    Instant thoiGianVao,
    Instant thoiGianRa,
    String trangThaiVao,
    String trangThaiRa,
    String ghiChu
) {
}
