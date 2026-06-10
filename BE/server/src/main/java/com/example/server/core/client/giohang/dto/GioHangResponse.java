package com.example.server.core.client.giohang.dto;

import java.math.BigDecimal;
import java.util.List;

/** Giỏ hàng của khách: danh sách dòng, tổng số lượng và tổng tiền. */
public record GioHangResponse(
        Integer id,
        List<GioHangItemResponse> items,
        int tongSoLuong,
        BigDecimal tongTien
) {}
