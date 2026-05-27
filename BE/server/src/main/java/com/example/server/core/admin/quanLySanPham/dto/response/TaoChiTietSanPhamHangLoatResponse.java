package com.example.server.core.admin.quanLySanPham.dto.response;

import java.util.List;

public record TaoChiTietSanPhamHangLoatResponse(
        GiayDetailResponse giay,
        List<BienTheResponse> bienThes,
        Boolean taoMoiSanPham
) {}
