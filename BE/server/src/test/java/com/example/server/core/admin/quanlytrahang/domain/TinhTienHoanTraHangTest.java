package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TinhTienHoanTraHangTest {

    @Test
    void phanBoGiamGiaTheoGiaTriDongSanPham() {
        BigDecimal tienHoan = TinhTienHoanTraHang.tinh(
                new BigDecimal("300000"),
                1,
                new BigDecimal("1000000"),
                new BigDecimal("100000")
        );

        assertEquals(new BigDecimal("270000.00"), tienHoan);
    }

    @Test
    void khongChoSoLuongChapNhanBangKhong() {
        assertThrows(BusinessException.class, () -> TinhTienHoanTraHang.tinh(
                new BigDecimal("300000"),
                0,
                new BigDecimal("1000000"),
                new BigDecimal("100000")
        ));
    }

    @Test
    void khongDeTienHoanAmKhiGiamGiaBangTongTienHang() {
        BigDecimal tienHoan = TinhTienHoanTraHang.tinh(
                new BigDecimal("300000"),
                1,
                new BigDecimal("1000000"),
                new BigDecimal("1000000")
        );

        assertEquals(new BigDecimal("0.00"), tienHoan);
    }
}
