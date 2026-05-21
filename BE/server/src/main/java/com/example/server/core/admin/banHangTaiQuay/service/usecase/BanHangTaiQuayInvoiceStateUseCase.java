package com.example.server.core.admin.banHangTaiQuay.service.usecase;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ThongTinGiaoHangTaiQuayRequest;
import org.springframework.stereotype.Component;

@Component
public class BanHangTaiQuayInvoiceStateUseCase {

    private static final int KENH_BAN_TAI_QUAY = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_XAC_NHAN = 1;
    private static final int TRANG_THAI_HOA_DON_CHO_GIAO_HANG = 2;
    private static final int TRANG_THAI_HOA_DON_HOAN_THANH = 5;
    private static final int TRANG_THAI_HOA_DON_HUY = 6;

    public boolean kenhBanTaiQuay(Integer kenhBan) {
        return kenhBan != null && kenhBan == KENH_BAN_TAI_QUAY;
    }

    public boolean trangThaiHoaDonCho(Integer trangThai) {
        return trangThai != null && trangThai == TRANG_THAI_HOA_DON_CHO_XAC_NHAN;
    }

    public String labelTrangThaiHoaDon(Integer trangThai) {
        return switch (trangThai) {
            case TRANG_THAI_HOA_DON_CHO_XAC_NHAN -> "Chờ xác nhận";
            case TRANG_THAI_HOA_DON_CHO_GIAO_HANG -> "Chờ giao hàng";
            case TRANG_THAI_HOA_DON_HOAN_THANH -> "Hoàn thành";
            case TRANG_THAI_HOA_DON_HUY -> "Hủy";
            default -> "Chờ xác nhận";
        };
    }

    public Integer xacDinhTrangThaiSauThanhToan(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        return thongTinGiaoHang != null && Boolean.TRUE.equals(thongTinGiaoHang.giaoHang())
                ? TRANG_THAI_HOA_DON_CHO_GIAO_HANG
                : TRANG_THAI_HOA_DON_HOAN_THANH;
    }
}
