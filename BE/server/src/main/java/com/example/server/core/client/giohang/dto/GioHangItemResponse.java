package com.example.server.core.client.giohang.dto;

import java.math.BigDecimal;

/** Một dòng sản phẩm trong giỏ hàng. */
public record GioHangItemResponse(
        Integer id,
        Integer giayChiTietId,
        Integer giayId,
        String tenSanPham,
        String mauSac,
        String kichCo,
        String hinhAnh,
        BigDecimal giaBan,
        Integer soLuong,
        Integer tonKho
) {}
