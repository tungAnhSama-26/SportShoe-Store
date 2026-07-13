package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ThongTinGiaoHangTaiQuayRequest;
import org.springframework.stereotype.Component;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.*;

@Component
public class TrangThaiHoaDonTaiQuayService {

    public boolean kenhBanTaiQuay(Integer kenhBan) {
        return kenhBan != null && kenhBan == KENH_BAN_TAI_QUAY;
    }

    public boolean trangThaiHoaDonCho(Integer trangThai) {
        return trangThai != null && trangThai == TRANG_THAI_HOA_DON_CHO_TAI_QUAY;
    }

    public String labelTrangThaiHoaDon(Integer trangThai) {
        return switch (trangThai) {
            case TRANG_THAI_HOA_DON_CHO_XAC_NHAN -> "Chờ xác nhận";
            case TRANG_THAI_HOA_DON_CHO_TAI_QUAY -> "Hóa đơn chờ";
            case TRANG_THAI_HOA_DON_CHO_GIAO_HANG -> "Chờ lấy hàng";
            case TRANG_THAI_HOA_DON_HOAN_THANH -> "Hoàn thành";
            case TRANG_THAI_HOA_DON_HUY -> "Hủy";
            case TRANG_THAI_HOA_DON_DA_XAC_NHAN -> "Đã xác nhận";
            default -> "Hóa đơn chờ";
        };
    }

    public Integer xacDinhTrangThaiSauThanhToan(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        return thongTinGiaoHang != null && Boolean.TRUE.equals(thongTinGiaoHang.giaoHang())
                ? TRANG_THAI_HOA_DON_DA_XAC_NHAN
                : TRANG_THAI_HOA_DON_HOAN_THANH;
    }
}
