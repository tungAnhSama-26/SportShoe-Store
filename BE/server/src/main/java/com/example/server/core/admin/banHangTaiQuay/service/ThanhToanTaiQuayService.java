package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.*;

@Component
public class ThanhToanTaiQuayService {

    public BigDecimal xacDinhTienKhachDua(Integer hinhThuc, BigDecimal tienKhachDua, BigDecimal tongTien) {
        if (hinhThuc == null) {
            throw new BusinessException("Hình thức thanh toán không hợp lệ");
        }

        if (hinhThuc == HINH_THUC_TIEN_MAT) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tongTien) < 0) {
                java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                String khachDuaStr = tienKhachDua != null ? formatter.format(tienKhachDua).replace(',', '.') : "0";
                String tongTienStr = tongTien != null ? formatter.format(tongTien).replace(',', '.') : "0";
                throw new BusinessException("Tiền khách đưa phải lớn hơn hoặc bằng tổng tiền (khách đưa: " + khachDuaStr + " ₫, tổng tiền: " + tongTienStr + " ₫)");
            }
            return tienKhachDua;
        }

        return tienKhachDua == null || tienKhachDua.compareTo(BigDecimal.ZERO) <= 0 ? tongTien : tienKhachDua;
    }

    public void validateTienKhachDua(BigDecimal tienKhachDua) {
        if (tienKhachDua != null && tienKhachDua.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tiền khách đưa không được âm");
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
            throw new BusinessException("Hình thức thanh toán không hợp lệ");
        }
        if (hinhThucUi == 4) {
            return HINH_THUC_CHUYEN_KHOAN;
        }
        if (hinhThucUi < HINH_THUC_TIEN_MAT || hinhThucUi > 4) {
            throw new BusinessException("Hình thức thanh toán không được hỗ trợ");
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
