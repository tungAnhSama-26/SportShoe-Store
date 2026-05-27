package com.example.server.core.admin.quanLySanPham.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ChiTietSanPhamListItemResponse(
        Integer id,
        Integer giayId,
        String maSanPham,
        String maChiTietSanPham,
        String sku,
        String tenSanPham,
        String thuongHieu,
        String loaiGiay,
        String chatLieu,
        Integer gioiTinh,
        Integer mauSacId,
        String mauSac,
        String maMauHex,
        Integer kichCoId,
        String kichCo,
        Integer soLuong,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        Integer kichHoat,
        String hinhAnh,
        Instant ngayTao,
        Instant ngayCapNhat,
        Integer dotGiamGiaId,
        String maDotGiamGia,
        String tenDotGiamGia,
        Integer loaiGiam,
        BigDecimal giaTriGiam
) {}
