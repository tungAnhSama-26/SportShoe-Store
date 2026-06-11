package com.example.server.core.admin.nhanVien.dto.responsse;

import java.time.LocalDate;
import java.util.UUID;

public record LichLamViecResponse(
        UUID id,
        UUID nhanVienId,
        LocalDate ngay,
        String ca
) {}
