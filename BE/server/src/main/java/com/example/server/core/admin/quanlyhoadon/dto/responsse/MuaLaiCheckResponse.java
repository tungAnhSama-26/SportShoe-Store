package com.example.server.core.admin.quanlyhoadon.dto.responsse;

import java.math.BigDecimal;
import java.util.List;

public record MuaLaiCheckResponse(
        boolean coTheMuaLai,
        List<String> warnings,
        List<MuaLaiItem> items
) {
    public record MuaLaiItem(
            Integer giayChiTietId,
            String tenSanPham,
            String phanLoai,
            String mauSac,
            String kichCo,
            Integer soLuong,
            BigDecimal giaBan,
            String hinhAnh,
            String sku,
            String maSanPham,
            Integer soLuongTon
    ) {}
}
