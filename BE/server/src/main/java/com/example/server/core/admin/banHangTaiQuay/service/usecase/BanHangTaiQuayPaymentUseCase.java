package com.example.server.core.admin.banHangTaiQuay.service.usecase;

import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class BanHangTaiQuayPaymentUseCase {

    private static final int HINH_THUC_TIEN_MAT = 1;
    private static final int HINH_THUC_CHUYEN_KHOAN = 2;
    private static final int HINH_THUC_VI = 3;

    public BigDecimal xacDinhTienKhachDua(Integer hinhThuc, BigDecimal tienKhachDua, BigDecimal tongTien) {
        if (hinhThuc == null) {
            throw new BusinessException("Hinh thuc thanh toan khong hop le");
        }

        if (hinhThuc == HINH_THUC_TIEN_MAT) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tongTien) < 0) {
                throw new BusinessException("Tien khach dua phai lon hon hoac bang tong tien");
            }
            return tienKhachDua;
        }

        return tienKhachDua == null || tienKhachDua.compareTo(BigDecimal.ZERO) <= 0 ? tongTien : tienKhachDua;
    }

    public void validateTienKhachDua(BigDecimal tienKhachDua) {
        if (tienKhachDua != null && tienKhachDua.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tien khach dua khong duoc am");
        }
    }

    public BigDecimal tinhTienThua(Integer hinhThuc, BigDecimal tienKhachDua, BigDecimal tongTien) {
        if (hinhThuc != null && hinhThuc == HINH_THUC_TIEN_MAT) {
            return tienKhachDua.subtract(tongTien);
        }
        return BigDecimal.ZERO;
    }

    public Integer mapHinhThucThanhToan(Integer hinhThucUi) {
        if (hinhThucUi == null) {
            throw new BusinessException("Hinh thuc thanh toan khong hop le");
        }
        if (hinhThucUi == 4) {
            return HINH_THUC_CHUYEN_KHOAN;
        }
        if (hinhThucUi < HINH_THUC_TIEN_MAT || hinhThucUi > 4) {
            throw new BusinessException("Hinh thuc thanh toan khong duoc ho tro");
        }
        return hinhThucUi == 3 ? HINH_THUC_VI : hinhThucUi;
    }

    public String resolveCongThanhToan(Integer hinhThucUi) {
        return switch (hinhThucUi) {
            case 2 -> "Chuyen khoan";
            case 3 -> "Vi dien tu";
            case 4 -> "The/POS";
            default -> null;
        };
    }
}
