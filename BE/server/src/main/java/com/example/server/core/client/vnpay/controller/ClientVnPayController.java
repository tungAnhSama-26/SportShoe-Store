package com.example.server.core.client.vnpay.controller;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.vnpay.dto.TaoMaVnPayResponse;
import com.example.server.core.client.vnpay.service.ClientVnPayService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thanh toán VNPay giả lập: tạo mã QR; khi khách quét mã (mở URL xác nhận) thì tự tạo đơn.
 */
@RestController
@RequestMapping("/api/v1/client/vnpay")
public class ClientVnPayController {

    private final ClientVnPayService service;

    public ClientVnPayController(ClientVnPayService service) {
        this.service = service;
    }

    /** Tạo mã QR thanh toán cho đơn (chưa tạo đơn). FE dùng qrData để vẽ QR. */
    @PostMapping("/tao-ma")
    public ResponseEntity<ApiResponse<TaoMaVnPayResponse>> taoMa(
            @Valid @RequestBody DatHangRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = service.taoMa(request);
        // Dùng IP LAN của máy chủ (không dùng localhost) để điện thoại quét QR truy cập được.
        String host = layIpLan(httpRequest);
        String qrData = "http://" + host + ":" + httpRequest.getServerPort()
                + "/api/v1/client/vnpay/xac-nhan/" + token;
        String maGiaoDich = "VNPAY" + token.substring(0, 10).toUpperCase();
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo mã thanh toán thành công",
                new TaoMaVnPayResponse(token, qrData, maGiaoDich)
        ));
    }

    /** Mở khi quét QR (trên điện thoại): tạo đơn + hiển thị trang xác nhận. */
    @GetMapping(value = "/xac-nhan/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> xacNhan(@PathVariable String token) {
        String maHoaDon = service.xacNhan(token);
        String html = """
                <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Thanh toán thành công</title></head>
                <body style="margin:0;font-family:Arial,sans-serif;background:#f4f4f7;display:flex;align-items:center;justify-content:center;height:100vh;">
                  <div style="background:#fff;border-radius:20px;padding:40px 32px;text-align:center;box-shadow:0 12px 30px rgba(0,0,0,.08);max-width:360px;">
                    <div style="width:72px;height:72px;border-radius:999px;background:#dcfce7;color:#16a34a;font-size:40px;line-height:72px;margin:0 auto 16px;">&#10004;</div>
                    <h1 style="font-size:22px;color:#16a34a;margin:0 0 8px;">Thanh toán thành công</h1>
                    <p style="color:#6b7280;font-size:15px;margin:0;">Đơn hàng <b>__MA__</b> đã được tạo.<br>Bạn có thể quay lại trang mua sắm.</p>
                  </div>
                </body></html>
                """.replace("__MA__", maHoaDon == null ? "" : maHoaDon);
        return ResponseEntity.ok(html);
    }

    /** FE poll: trạng thái thanh toán + mã hóa đơn nếu đã tạo. */
    @GetMapping("/trang-thai/{token}")
    public ResponseEntity<ApiResponse<Map<String, String>>> trangThai(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success("OK", service.trangThai(token)));
    }

    /**
     * Tìm IPv4 LAN của máy chủ để QR quét được từ điện thoại.
     * Ưu tiên 192.168.x (Wi-Fi/LAN thật) > 10.x > 172.16-31.x (thường là adapter ảo Hyper-V/Docker).
     */
    private String layIpLan(HttpServletRequest fallback) {
        String ip192 = null;
        String ip10 = null;
        String ip172 = null;
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if (ip.startsWith("192.168.") && ip192 == null) {
                        ip192 = ip;
                    } else if (ip.startsWith("10.") && ip10 == null) {
                        ip10 = ip;
                    } else if (ip.matches("172\\.(1[6-9]|2\\d|3[01])\\..*") && ip172 == null) {
                        ip172 = ip;
                    }
                }
            }
        } catch (Exception ignored) {
            // Không lấy được IP LAN -> dùng host từ request.
        }
        if (ip192 != null) {
            return ip192;
        }
        if (ip10 != null) {
            return ip10;
        }
        if (ip172 != null) {
            return ip172;
        }
        return fallback.getServerName();
    }
}
