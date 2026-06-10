package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrangThaiPhieuTraHangTest {

    @Test
    void choDuyetCoTheChuyenSangChoKhachGuiHang() {
        assertDoesNotThrow(() -> TrangThaiPhieuTraHang.CHO_DUYET
                .kiemTraCoTheChuyenSang(TrangThaiPhieuTraHang.CHO_KHACH_GUI_HANG));
    }

    @Test
    void daNhanHangKhongDuocChuyenThangSangHoanTat() {
        assertThrows(BusinessException.class, () -> TrangThaiPhieuTraHang.DA_NHAN_HANG
                .kiemTraCoTheChuyenSang(TrangThaiPhieuTraHang.HOAN_TAT));
    }

    @Test
    void choHoanTienChiCoTheHoanTatSauKhiHoanTienThanhCong() {
        assertDoesNotThrow(() -> TrangThaiPhieuTraHang.CHO_HOAN_TIEN
                .kiemTraCoTheChuyenSang(TrangThaiPhieuTraHang.HOAN_TAT));
        assertThrows(BusinessException.class, () -> TrangThaiPhieuTraHang.CHO_HOAN_TIEN
                .kiemTraCoTheChuyenSang(TrangThaiPhieuTraHang.DANG_KIEM_TRA));
    }
}
