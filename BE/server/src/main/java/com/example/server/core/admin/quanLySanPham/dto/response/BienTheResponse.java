package com.example.server.core.admin.quanLySanPham.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record BienTheResponse(
        Integer id,
        String maBienThe,
        String sku,
        Integer soLuong,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        Integer kichHoat,
        Integer mauSacId,
        String mauSac,
        String maMauHex,
        Integer kichCoId,
        String kichCo,
        Instant ngayTao,
        Instant ngayCapNhat,
        Integer dotGiamGiaId,
        String maDotGiamGia,
        String tenDotGiamGia,
        Integer loaiGiam,
        BigDecimal giaTriGiam
) {}
