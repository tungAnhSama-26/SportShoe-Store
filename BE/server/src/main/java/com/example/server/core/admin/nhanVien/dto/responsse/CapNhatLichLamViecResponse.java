package com.example.server.core.admin.nhanVien.dto.responsse;

import java.time.LocalDate;

public record CapNhatLichLamViecResponse(
        LocalDate tuNgay,
        LocalDate denNgay,
        long soLichDaXoa,
        int soLichDaTao,
        int soCaChuaCoNhanVien
) {
}
