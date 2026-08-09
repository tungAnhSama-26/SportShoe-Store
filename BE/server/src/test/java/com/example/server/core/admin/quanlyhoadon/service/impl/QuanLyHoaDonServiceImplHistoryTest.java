package com.example.server.core.admin.quanlyhoadon.service.impl;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class QuanLyHoaDonServiceImplHistoryTest {

    @Test
    void taoGhiChuCapNhatGiaoHang_ghiDungMotTruongThayDoi() {
        Optional<String> ketQua = QuanLyHoaDonServiceImpl.taoGhiChuCapNhatGiaoHang(
                "Nguyễn Văn A",
                "Nguyễn Văn B",
                "0912345678",
                "0912345678",
                "Số 1, Xã A, Hà Nội",
                "Số 1, Xã A, Hà Nội"
        );

        assertThat(ketQua).contains(
                "Nhân viên cập nhật thông tin giao hàng:\n"
                        + "- Tên người nhận: 'Nguyễn Văn A' → 'Nguyễn Văn B'"
        );
    }

    @Test
    void taoGhiChuCapNhatGiaoHang_ghiMoiThayDoiTrenMotDong() {
        Optional<String> ketQua = QuanLyHoaDonServiceImpl.taoGhiChuCapNhatGiaoHang(
                "Nguyễn Văn A",
                "Nguyễn Văn B",
                "0912345678",
                "0987654321",
                "Số 1, Xã A, Hà Nội",
                "Số 2, Xã B, Hà Nội"
        );

        assertThat(ketQua).hasValueSatisfying(ghiChu -> assertThat(ghiChu)
                .contains("- Tên người nhận: 'Nguyễn Văn A' → 'Nguyễn Văn B'")
                .contains("- SĐT: '0912345678' → '0987654321'")
                .contains("- Địa chỉ: 'Số 1, Xã A, Hà Nội' → 'Số 2, Xã B, Hà Nội'")
                .hasLineCount(4));
    }

    @Test
    void taoGhiChuCapNhatGiaoHang_khongTaoGhiChuKhiDuLieuKhongDoi() {
        Optional<String> ketQua = QuanLyHoaDonServiceImpl.taoGhiChuCapNhatGiaoHang(
                "Nguyễn Văn A",
                " Nguyễn Văn A ",
                "0912345678",
                "0912345678",
                "Số 1, Xã A, Hà Nội",
                "Số 1, Xã A, Hà Nội"
        );

        assertThat(ketQua).isEmpty();
    }
}
