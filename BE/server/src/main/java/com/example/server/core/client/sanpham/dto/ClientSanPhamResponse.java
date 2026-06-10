package com.example.server.core.client.sanpham.dto;

import com.example.server.core.admin.quanLySanPham.dto.response.GiayListItemResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Sản phẩm cho trang danh sách phía khách hàng: thông tin cơ bản (tái dùng GiayListItemResponse)
 * kèm màu sắc/kích cỡ để lọc và giá sau khi áp đợt giảm giá.
 */
public record ClientSanPhamResponse(
        GiayListItemResponse thongTin,
        String hinhAnhSanPham,
        BigDecimal giaHienThiMin,
        boolean coGiam,
        List<String> mauSac,
        List<String> kichCo
) {}
