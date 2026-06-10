package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.entity.HoaDon;
import com.example.server.entity.PhieuTraHang;
import com.example.server.entity.ThanhToan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class GiaoDichHoanTienFactoryTest {

    @Test
    void taoGiaoDichHoanMoiVaKhongGhiDeGiaoDichGoc() {
        HoaDon hoaDon = new HoaDon();
        PhieuTraHang phieuTraHang = new PhieuTraHang();
        ThanhToan giaoDichGoc = new ThanhToan();
        giaoDichGoc.setHoaDon(hoaDon);
        giaoDichGoc.setSoTien(new BigDecimal("1000000"));
        giaoDichGoc.setHinhThuc(2);
        giaoDichGoc.setTrangThai(1);
        giaoDichGoc.setNgayTao(Instant.parse("2026-06-01T00:00:00Z"));

        ThanhToan giaoDichHoan = GiaoDichHoanTienFactory.tao(
                giaoDichGoc,
                phieuTraHang,
                new BigDecimal("300000"),
                2,
                "RF001",
                "Hoàn tiền trả hàng"
        );

        assertNotSame(giaoDichGoc, giaoDichHoan);
        assertSame(giaoDichGoc, giaoDichHoan.getGiaoDichGoc());
        assertSame(phieuTraHang, giaoDichHoan.getPhieuTraHang());
        assertEquals(2, giaoDichHoan.getLoaiGiaoDich());
        assertEquals(new BigDecimal("300000"), giaoDichHoan.getSoTien());
        assertEquals(new BigDecimal("1000000"), giaoDichGoc.getSoTien());
        assertEquals(1, giaoDichGoc.getTrangThai());
        assertNull(giaoDichGoc.getPhieuTraHang());
    }
}
