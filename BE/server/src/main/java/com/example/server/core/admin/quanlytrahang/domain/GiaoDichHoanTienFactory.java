package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.entity.PhieuTraHang;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;

public final class GiaoDichHoanTienFactory {

    private static final int LOAI_GIAO_DICH_HOAN_TIEN = 2;
    private static final int TRANG_THAI_DA_HOAN_TIEN = 5;

    private GiaoDichHoanTienFactory() {
    }

    public static ThanhToan tao(
            ThanhToan giaoDichGoc,
            PhieuTraHang phieuTraHang,
            BigDecimal soTienHoan,
            Integer hinhThucHoan,
            String maGiaoDich,
            String ghiChu
    ) {
        if (giaoDichGoc == null || giaoDichGoc.getHoaDon() == null) {
            throw new BusinessException("Không tìm thấy giao dịch thanh toán gốc hợp lệ");
        }
        if (soTienHoan == null || soTienHoan.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền hoàn phải lớn hơn 0");
        }
        if (hinhThucHoan == null) {
            throw new BusinessException("Vui lòng chọn hình thức hoàn tiền");
        }

        Instant now = Instant.now();
        ThanhToan giaoDichHoan = new ThanhToan();
        giaoDichHoan.setHoaDon(giaoDichGoc.getHoaDon());
        giaoDichHoan.setNhanVien(giaoDichGoc.getNhanVien());
        giaoDichHoan.setGiaoDichGoc(giaoDichGoc);
        giaoDichHoan.setPhieuTraHang(phieuTraHang);
        giaoDichHoan.setLoaiGiaoDich(LOAI_GIAO_DICH_HOAN_TIEN);
        giaoDichHoan.setHinhThuc(hinhThucHoan);
        giaoDichHoan.setSoTien(soTienHoan);
        giaoDichHoan.setMaGiaoDich(maGiaoDich);
        giaoDichHoan.setCongThanhToan("Hoàn tiền trả hàng");
        giaoDichHoan.setTrangThai(TRANG_THAI_DA_HOAN_TIEN);
        giaoDichHoan.setGhiChu(ghiChu);
        giaoDichHoan.setNgayThanhToan(now);
        giaoDichHoan.setNgayTao(now);
        return giaoDichHoan;
    }

    public static ThanhToan taoKhongQuaPhieuTraHang(
            ThanhToan giaoDichGoc,
            BigDecimal soTienHoan,
            Integer hinhThucHoan,
            String maGiaoDich,
            String ghiChu
    ) {
        return tao(
                giaoDichGoc,
                null,
                soTienHoan,
                hinhThucHoan,
                maGiaoDich,
                ghiChu
        );
    }
}
