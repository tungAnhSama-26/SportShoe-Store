package com.example.server.core.client.vnpay.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.dathang.service.ClientDatHangService;
import com.example.server.infrastructure.exception.BusinessException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Thanh toán VNPay GIẢ LẬP (không qua cổng thật): tạo mã QR, khi mã được "quét"
 * (mở URL xác nhận) thì tự động tạo đơn hàng. Phiên lưu tạm trong bộ nhớ.
 */
@Service
public class ClientVnPayService {

    public static final String TRANG_THAI_CHO = "CHO";
    public static final String TRANG_THAI_DA_THANH_TOAN = "DA_THANH_TOAN";
    public static final String TRANG_THAI_KHONG_TON_TAI = "KHONG_TON_TAI";

    private final ClientDatHangService datHangService;
    private final Map<String, Phien> phienMap = new ConcurrentHashMap<>();

    public ClientVnPayService(ClientDatHangService datHangService) {
        this.datHangService = datHangService;
    }

    private static class Phien {
        final DatHangRequest request;
        final String maGiaoDich;
        volatile String trangThai = TRANG_THAI_CHO;
        volatile String maHoaDon;

        Phien(DatHangRequest request, String maGiaoDich) {
            this.request = request;
            this.maGiaoDich = maGiaoDich;
        }
    }

    /** Tạo token cho 1 lần thanh toán; chưa tạo đơn. */
    public String taoMa(DatHangRequest request) {
        String token = UUID.randomUUID().toString().replace("-", "");
        phienMap.put(token, new Phien(request, taoMaGiaoDich(token)));
        return token;
    }

    /** Khi mã được quét: tạo đơn hàng (1 lần) và đánh dấu đã thanh toán. */
    public synchronized String xacNhan(String token) {
        Phien phien = phienMap.get(token);
        if (phien == null) {
            throw new BusinessException("Mã thanh toán không tồn tại hoặc đã hết hạn");
        }
        if (!TRANG_THAI_DA_THANH_TOAN.equals(phien.trangThai)) {
            DatHangResponse ketQua = datHangService.datHang(phien.request, phien.maGiaoDich);
            phien.maHoaDon = ketQua.maHoaDon();
            phien.trangThai = TRANG_THAI_DA_THANH_TOAN;
        }
        return phien.maHoaDon;
    }

    /** Trạng thái phiên thanh toán (FE poll). */
    public Map<String, String> trangThai(String token) {
        Phien phien = phienMap.get(token);
        Map<String, String> result = new HashMap<>();
        if (phien == null) {
            result.put("trangThai", TRANG_THAI_KHONG_TON_TAI);
            return result;
        }
        result.put("trangThai", phien.trangThai);
        if (phien.maHoaDon != null) {
            result.put("maHoaDon", phien.maHoaDon);
        }
        return result;
    }

    public String layMaGiaoDich(String token) {
        Phien phien = phienMap.get(token);
        return phien != null ? phien.maGiaoDich : taoMaGiaoDich(token);
    }

    private String taoMaGiaoDich(String token) {
        String normalized = token == null ? "" : token.replace("-", "");
        String suffix = normalized.substring(0, Math.min(10, normalized.length()))
                .toUpperCase(Locale.ROOT);
        return "VNPAY" + suffix;
    }
}
