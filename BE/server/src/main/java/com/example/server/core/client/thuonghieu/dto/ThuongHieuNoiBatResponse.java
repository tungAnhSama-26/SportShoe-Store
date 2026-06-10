package com.example.server.core.client.thuonghieu.dto;

/** Hãng nổi bật hiển thị ở trang chủ khách hàng. */
public record ThuongHieuNoiBatResponse(
        Integer id,
        String ten,
        String moTa,
        String logoUrl
) {}
