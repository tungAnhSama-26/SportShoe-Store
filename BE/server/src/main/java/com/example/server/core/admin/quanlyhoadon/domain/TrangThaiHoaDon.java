package com.example.server.core.admin.quanlyhoadon.domain;

import com.example.server.infrastructure.exception.BusinessException;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum TrangThaiHoaDon {
    CHO_XAC_NHAN(1, "Chờ xác nhận"),
    CHO_LAY_HANG(2, "Chờ lấy hàng"),
    DANG_GIAO_HANG(3, "Đang giao hàng"),
    DA_GIAO_HANG(4, "Đã giao hàng"),
    HOAN_THANH(5, "Hoàn thành"),
    HUY(6, "Hủy"),
    YEU_CAU_HUY(7, "Yêu cầu hủy"),
    CAN_HOAN_TIEN_LEGACY(8, "Cần hoàn tiền"),
    DA_XAC_NHAN(9, "Đã xác nhận"),
    GIAO_HANG_THAT_BAI(10, "Giao hàng thất bại"),
    HOA_DON_CHO(11, "Hóa đơn chờ");

    private final int ma;
    private final String ten;

    TrangThaiHoaDon(int ma, String ten) {
        this.ma = ma;
        this.ten = ten;
    }

    public int getMa() {
        return ma;
    }

    public String getTen() {
        return ten;
    }

    public static TrangThaiHoaDon tuMa(Integer ma) {
        if (ma != null) {
            for (TrangThaiHoaDon trangThai : values()) {
                if (trangThai.ma == ma) {
                    return trangThai;
                }
            }
        }
        throw new BusinessException("Trạng thái hóa đơn không hợp lệ");
    }

    public static TrangThaiHoaDon tuNhan(String nhan) {
        if (nhan != null) {
            String normalized = nhan.trim().toLowerCase(Locale.ROOT);
            for (TrangThaiHoaDon trangThai : values()) {
                if (trangThai.ten.toLowerCase(Locale.ROOT).equals(normalized)) {
                    return trangThai;
                }
            }
            if ("chờ giao hàng".equals(normalized)) {
                return DANG_GIAO_HANG;
            }
        }
        throw new BusinessException("Trạng thái hóa đơn không hợp lệ");
    }

    public void kiemTraCoTheChuyenSang(TrangThaiHoaDon trangThaiMoi, boolean donTaiQuay) {
        if (this == trangThaiMoi) {
            return;
        }
        if (this == CAN_HOAN_TIEN_LEGACY) {
            throw new BusinessException("Trạng thái cần hoàn tiền cũ phải được xử lý qua lịch sử thanh toán");
        }
        if (donTaiQuay && this == CHO_XAC_NHAN && trangThaiMoi == HOAN_THANH) {
            return;
        }
        if (donTaiQuay && this == HOA_DON_CHO && trangThaiMoi == HOAN_THANH) {
            return;
        }
        if (!cacTrangThaiKeTiep().contains(trangThaiMoi)) {
            throw new BusinessException(
                    "Không thể chuyển hóa đơn từ " + ten + " sang " + trangThaiMoi.ten
            );
        }
    }

    private Set<TrangThaiHoaDon> cacTrangThaiKeTiep() {
        return switch (this) {
            case CHO_XAC_NHAN -> EnumSet.of(DA_XAC_NHAN, HUY, YEU_CAU_HUY);
            case DA_XAC_NHAN -> EnumSet.of(CHO_LAY_HANG, HUY);
            case CHO_LAY_HANG -> EnumSet.of(DANG_GIAO_HANG, HUY);
            case DANG_GIAO_HANG -> EnumSet.of(DA_GIAO_HANG, GIAO_HANG_THAT_BAI, HUY);
            case DA_GIAO_HANG -> EnumSet.of(HOAN_THANH, GIAO_HANG_THAT_BAI, HUY);
            case GIAO_HANG_THAT_BAI -> EnumSet.of(DANG_GIAO_HANG, HUY);
            case YEU_CAU_HUY -> EnumSet.of(HUY, CHO_XAC_NHAN, DA_XAC_NHAN, CHO_LAY_HANG);
            case HOA_DON_CHO -> EnumSet.of(HOAN_THANH, CHO_XAC_NHAN, DA_XAC_NHAN, CHO_LAY_HANG, HUY);
            case HOAN_THANH, HUY, CAN_HOAN_TIEN_LEGACY -> EnumSet.noneOf(TrangThaiHoaDon.class);
        };
    }
}
