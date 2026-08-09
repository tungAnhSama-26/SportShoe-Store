package com.example.server.core.admin.quanlyhoadon.dto.responsse;

import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TinhPhiVanChuyenGhnResponse(
        BigDecimal phiVanChuyen,
        Integer total,
        Integer serviceFee,
        Integer insuranceFee,
        Integer pickStationFee,
        Integer couponValue,
        DiaChiHaiCapResponse diaChiDaDoiSoat,
        boolean uocTinh,
        String nguonTinhPhi,
        boolean giaCu,
        Instant thoiDiemBaoGia,
        LocalDate ngayHieuLucBangGia
) {
}
