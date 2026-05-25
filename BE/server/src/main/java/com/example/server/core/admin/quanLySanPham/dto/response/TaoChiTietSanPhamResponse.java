package com.example.server.core.admin.quanLySanPham.dto.response;

public record TaoChiTietSanPhamResponse(
        GiayDetailResponse giay,
        BienTheResponse bienThe,
        Boolean taoMoiSanPham
) {}
