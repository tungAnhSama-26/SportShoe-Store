package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraHangValidatorTest {

    @Test
    void chiChoPhepTraDonDaGiaoHoacHoanThanh() {
        assertDoesNotThrow(() -> TraHangValidator.kiemTraTrangThaiHoaDon(4));
        assertDoesNotThrow(() -> TraHangValidator.kiemTraTrangThaiHoaDon(5));
        assertThrows(BusinessException.class, () -> TraHangValidator.kiemTraTrangThaiHoaDon(2));
    }

    @Test
    void tongSoLuongTraKhongDuocVuotSoLuongDaMua() {
        assertDoesNotThrow(() -> TraHangValidator.kiemTraSoLuong(3, 1, 2));
        assertThrows(BusinessException.class, () -> TraHangValidator.kiemTraSoLuong(3, 2, 2));
    }

    @Test
    void soLuongYeuCauPhaiLonHonKhong() {
        assertThrows(BusinessException.class, () -> TraHangValidator.kiemTraSoLuong(3, 0, 0));
    }
}
