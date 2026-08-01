package com.example.server.core.admin.nhanVien.dto.responsse;

import java.util.List;
import java.util.UUID;

public record GiaoCaOptionsResponse(
        boolean coTheKetCa,
        String caKeTiep,
        List<NhanVienNhanCaResponse> nhanVienNhanCa
) {
    public record NhanVienNhanCaResponse(UUID id, String ma, String hoTen, Integer vaiTro) {
    }
}
