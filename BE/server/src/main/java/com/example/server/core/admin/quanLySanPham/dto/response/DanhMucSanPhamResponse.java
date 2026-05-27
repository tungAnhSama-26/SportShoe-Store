package com.example.server.core.admin.quanLySanPham.dto.response;

import java.util.List;

public record DanhMucSanPhamResponse(
        List<LoaiGiayOption> loaiGiay,
        List<ThuongHieuOption> thuongHieu,
        List<MauSacOption> mauSac,
        List<KichCoOption> kichCo,
        List<DeGiayOption> deGiay,
        List<CoGiayOption> coGiay,
        List<ChatLieuGiayOption> chatLieuGiay,
        List<TrongLuongOption> trongLuong,
        List<CongNgheDemOption> congNgheDem
) {}
