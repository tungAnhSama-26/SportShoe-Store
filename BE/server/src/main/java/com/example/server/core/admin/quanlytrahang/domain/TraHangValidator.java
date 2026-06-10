package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;

public final class TraHangValidator {

    private static final int DA_GIAO_HANG = 4;
    private static final int HOAN_THANH = 5;

    private TraHangValidator() {
    }

    public static void kiemTraTrangThaiHoaDon(Integer trangThaiHoaDon) {
        if (trangThaiHoaDon == null
                || (trangThaiHoaDon != DA_GIAO_HANG && trangThaiHoaDon != HOAN_THANH)) {
            throw new BusinessException(
                    "Chỉ có thể tạo phiếu trả hàng cho hóa đơn đã giao hàng hoặc đã hoàn thành"
            );
        }
    }

    public static void kiemTraSoLuong(
            int soLuongDaMua,
            int soLuongYeuCauTra,
            int soLuongDaTra
    ) {
        if (soLuongDaMua <= 0) {
            throw new BusinessException("Số lượng sản phẩm đã mua không hợp lệ");
        }
        if (soLuongYeuCauTra <= 0) {
            throw new BusinessException("Số lượng yêu cầu trả phải lớn hơn 0");
        }
        if (soLuongDaTra < 0) {
            throw new BusinessException("Số lượng sản phẩm đã trả không hợp lệ");
        }
        if (soLuongDaTra + soLuongYeuCauTra > soLuongDaMua) {
            throw new BusinessException("Tổng số lượng trả không được vượt quá số lượng đã mua");
        }
    }
}
