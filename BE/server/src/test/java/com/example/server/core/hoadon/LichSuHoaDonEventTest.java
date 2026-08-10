package com.example.server.core.hoadon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LichSuHoaDonEventTest {

    @Test
    void chuanHoaMa_hoTroCaNhanCuVaMaMoi() {
        assertThat(LichSuHoaDonEvent.chuanHoaMa("Đã giao hàng"))
                .isEqualTo("DA_GIAO_HANG");
        assertThat(LichSuHoaDonEvent.chuanHoaMa("DA_GIAO_HANG"))
                .isEqualTo("DA_GIAO_HANG");
    }

    @Test
    void nhanHienThi_khongLamLoMaKyThuatRaGiaoDien() {
        assertThat(LichSuHoaDonEvent.nhanHienThi("KHACH_DA_NHAN_HANG"))
                .isEqualTo("Khách hàng đã nhận hàng");
        assertThat(LichSuHoaDonEvent.nhanHienThi("KHACH_SUA_DIA_CHI"))
                .isEqualTo("Cập nhật thông tin giao hàng");
    }

    @Test
    void trangThaiOnDinh_khongBaoGomYeuCauHuyVaSuKienPhu() {
        assertThat(LichSuHoaDonEvent.maTrangThaiOnDinh())
                .contains("CHO_XAC_NHAN", "DA_XAC_NHAN", "DA_GIAO_HANG")
                .doesNotContain("YEU_CAU_HUY", "KHACH_DA_NHAN_HANG");
    }
}
