package com.example.server.core.client.sepay.controller;

import com.example.server.core.client.vnpay.service.ClientVnPayService;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nhận webhook từ SePay khi có chuyển khoản vào tài khoản ngân hàng.
 *
 * <p>SePay gửi POST kèm header {@code Authorization: Apikey <key>}. Sau khi xác thực,
 * khớp nội dung chuyển khoản + số tiền với phiên thanh toán đang chờ rồi tạo đơn
 * (tái dùng {@link ClientVnPayService#xacNhanTheoChuyenKhoan}). Đây là nơi DUY NHẤT
 * được phép xác nhận thanh toán thật - không tin tưởng phía client.</p>
 */
@RestController
@RequestMapping("/api/v1/client/sepay")
public class ClientSePayController {

    private static final Logger log = LoggerFactory.getLogger(ClientSePayController.class);

    private final ClientVnPayService service;
    private final String apiKey;

    public ClientSePayController(
            ClientVnPayService service,
            @Value("${sepay.api-key:}") String apiKey
    ) {
        this.service = service;
        this.apiKey = apiKey;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        // 1. Xác thực: bắt buộc đúng API key của SePay (chấp nhận Apikey/Bearer/key trần).
        if (!xacThucDung(authorization)) {
            log.warn("SePay webhook xac thuc THAT BAI. Authorization nhan duoc: {}", anToanHeader(authorization));
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        // 2. Chỉ xử lý giao dịch TIỀN VÀO.
        if (!"in".equalsIgnoreCase(String.valueOf(body.get("transferType")))) {
            return ResponseEntity.ok(Map.of("success", true));
        }

        // 3. Lấy nội dung CK (ưu tiên mã SePay đã bóc tách) + số tiền.
        Object code = body.get("code");
        String noiDung = (code != null && !String.valueOf(code).isBlank())
                ? String.valueOf(code)
                : String.valueOf(body.getOrDefault("content", ""));
        long soTien = body.get("transferAmount") instanceof Number n ? n.longValue() : 0L;

        // 4. Khớp đơn + tạo đơn (idempotent). Luôn trả 200 để SePay không gửi lại.
        service.xacNhanTheoChuyenKhoan(noiDung, soTien);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * So khớp API key, chấp nhận các định dạng header phổ biến SePay có thể gửi:
     * "Apikey <key>", "Bearer <key>" (không phân biệt hoa thường tiền tố) hoặc key trần.
     */
    private boolean xacThucDung(String authorization) {
        if (apiKey == null || apiKey.isBlank() || authorization == null || authorization.isBlank()) {
            return false;
        }
        String value = authorization.trim();
        String lower = value.toLowerCase(Locale.ROOT);
        for (String prefix : new String[]{"apikey ", "bearer "}) {
            if (lower.startsWith(prefix)) {
                value = value.substring(prefix.length()).trim();
                break;
            }
        }
        return value.equals(apiKey);
    }

    /** Rút gọn header khi ghi log để không lộ trọn key trong log. */
    private String anToanHeader(String header) {
        if (header == null || header.isBlank()) {
            return "(khong co header)";
        }
        return header.length() <= 20 ? header : header.substring(0, 20) + "...";
    }
}
