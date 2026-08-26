package com.example.server.core.client.sepay.controller;

import com.example.server.core.admin.banHangTaiQuay.service.BanHangTaiQuayService;
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
 * khớp nội dung chuyển khoản + số tiền với phiên thanh toán Online hoặc Hóa đơn chờ tại quầy (POS).
 * Luôn trả 200 OK để SePay không gửi lại.</p>
 */
@RestController
@RequestMapping("/api/v1/client/sepay")
public class ClientSePayController {

    private static final Logger log = LoggerFactory.getLogger(ClientSePayController.class);

    private final ClientVnPayService clientVnPayService;
    private final BanHangTaiQuayService banHangTaiQuayService;
    private final String apiKey;

    public ClientSePayController(
            ClientVnPayService clientVnPayService,
            BanHangTaiQuayService banHangTaiQuayService,
            @Value("${sepay.api-key:}") String apiKey
    ) {
        this.clientVnPayService = clientVnPayService;
        this.banHangTaiQuayService = banHangTaiQuayService;
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
        Object content = body.get("content");
        Object description = body.get("description");

        StringBuilder fullContent = new StringBuilder();
        if (code != null && !String.valueOf(code).isBlank()) {
            fullContent.append(code).append(" ");
        }
        if (content != null && !String.valueOf(content).isBlank()) {
            fullContent.append(content).append(" ");
        }
        if (description != null && !String.valueOf(description).isBlank()) {
            fullContent.append(description);
        }
        String noiDung = fullContent.toString().trim();

        long soTien = body.get("transferAmount") instanceof Number n ? n.longValue() : 0L;

        log.info("SePay webhook nhan thong tin chuyen khoan: noiDung='{}', soTien={}", noiDung, soTien);

        // 4.1. Khớp đơn Online (Web Khách hàng).
        // Lỗi ở nhánh này không được chặn nhánh POS: tiền đã về, phải thử khớp nốt hóa đơn tại quầy.
        String onlineMa = null;
        try {
            onlineMa = clientVnPayService.xacNhanTheoChuyenKhoan(noiDung, soTien);
        } catch (RuntimeException e) {
            log.error("SePay webhook loi khi doi chieu don Online (noiDung='{}')", noiDung, e);
        }
        if (onlineMa != null) {
            log.info("SePay webhook xac nhan thanh toan don hang Online thanh cong: {}", onlineMa);
            return ResponseEntity.ok(Map.of("success", true, "orderCode", onlineMa, "type", "ONLINE"));
        }

        // 4.2. Khớp đơn Bán hàng tại quầy (POS)
        String posMa = null;
        try {
            posMa = banHangTaiQuayService.xacNhanThanhToanSePay(noiDung, soTien);
        } catch (RuntimeException e) {
            // Không ném ra ngoài để SePay khỏi gửi lại liên tục; thu ngân vẫn còn nút "Đã thanh toán".
            log.error("SePay webhook loi khi doi chieu don Tai Quay (noiDung='{}')", noiDung, e);
        }
        if (posMa != null) {
            log.info("SePay webhook xac nhan thanh toan don hang Tai Quay (POS) thanh cong: {}", posMa);
            return ResponseEntity.ok(Map.of("success", true, "orderCode", posMa, "type", "POS"));
        }

        log.info("SePay webhook nhan thong tin nhung khong khop hoa don nao dang cho.");
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
