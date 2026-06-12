package com.example.server.core.client.thongke.dto;

/** Số liệu thật hiển thị trên banner trang chủ. */
public record ThongKeTrangChuResponse(
        long soKhachHang,
        long soSanPham,
        double diemTrungBinh,
        long soDanhGia
) {}
