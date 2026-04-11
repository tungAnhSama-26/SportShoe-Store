package com.example.server.core.admin.quanlyhoadon.dto.request;

import java.util.List;

public record CapNhatSanPhamHoaDonRequest(
        List<SanPhamItemRequest> items
) {
    public record SanPhamItemRequest(
            Integer chiTietId,
            Integer soLuong
    ) {}
}
