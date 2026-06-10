package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;

import java.util.EnumSet;
import java.util.Set;

public enum TrangThaiPhieuTraHang {
    CHO_DUYET(1, "Chờ duyệt"),
    CHO_KHACH_GUI_HANG(2, "Chờ khách gửi hàng"),
    DANG_HOAN_HANG(3, "Đang hoàn hàng"),
    DA_NHAN_HANG(4, "Đã nhận hàng"),
    DANG_KIEM_TRA(5, "Đang kiểm tra"),
    CHO_HOAN_TIEN(6, "Chờ hoàn tiền"),
    HOAN_TAT(7, "Hoàn tất"),
    TU_CHOI(8, "Từ chối"),
    DA_HUY(9, "Đã hủy"),
    HOAN_HANG_THAT_BAI(10, "Hoàn hàng thất bại");

    private final int ma;
    private final String ten;

    TrangThaiPhieuTraHang(int ma, String ten) {
        this.ma = ma;
        this.ten = ten;
    }

    public int getMa() {
        return ma;
    }

    public String getTen() {
        return ten;
    }

    public static TrangThaiPhieuTraHang tuMa(Integer ma) {
        if (ma != null) {
            for (TrangThaiPhieuTraHang trangThai : values()) {
                if (trangThai.ma == ma) {
                    return trangThai;
                }
            }
        }
        throw new BusinessException("Trạng thái phiếu trả hàng không hợp lệ");
    }

    public void kiemTraCoTheChuyenSang(TrangThaiPhieuTraHang trangThaiMoi) {
        if (!cacTrangThaiKeTiep().contains(trangThaiMoi)) {
            throw new BusinessException(
                    "Không thể chuyển phiếu trả hàng từ " + ten + " sang " + trangThaiMoi.ten
            );
        }
    }

    private Set<TrangThaiPhieuTraHang> cacTrangThaiKeTiep() {
        return switch (this) {
            case CHO_DUYET -> EnumSet.of(
                    CHO_KHACH_GUI_HANG,
                    DA_NHAN_HANG,
                    CHO_HOAN_TIEN,
                    TU_CHOI,
                    DA_HUY
            );
            case CHO_KHACH_GUI_HANG -> EnumSet.of(DANG_HOAN_HANG, DA_HUY);
            case DANG_HOAN_HANG -> EnumSet.of(DA_NHAN_HANG, HOAN_HANG_THAT_BAI);
            case HOAN_HANG_THAT_BAI -> EnumSet.of(DANG_HOAN_HANG, DA_HUY);
            case DA_NHAN_HANG -> EnumSet.of(DANG_KIEM_TRA);
            case DANG_KIEM_TRA -> EnumSet.of(CHO_HOAN_TIEN, TU_CHOI);
            case CHO_HOAN_TIEN -> EnumSet.of(HOAN_TAT);
            case HOAN_TAT, TU_CHOI, DA_HUY -> EnumSet.noneOf(TrangThaiPhieuTraHang.class);
        };
    }
}
