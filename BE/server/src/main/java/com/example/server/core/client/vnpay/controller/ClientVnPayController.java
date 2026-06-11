package com.example.server.core.client.vnpay.controller;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.vnpay.dto.TaoMaVnPayResponse;
import com.example.server.core.client.vnpay.service.ClientVnPayService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
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
 * Thanh toán bằng chuyển khoản thật qua VietQR (SePay): tạo mã QR; đơn chỉ được tạo
 * khi webhook SePay xác nhận tiền vào (xem ClientSePayController). Endpoint xác nhận
 * thủ công bên dưới chỉ dùng để TEST khi chưa muốn chuyển khoản thật.
 */
@RestController
@RequestMapping("/api/v1/client/vnpay")
public class ClientVnPayController {

    private final ClientVnPayService service;

    public ClientVnPayController(ClientVnPayService service) {
        this.service = service;
    }

    /** Tạo phiên + ảnh QR VietQR cho đơn (chưa tạo đơn). FE hiển thị qrData làm ảnh QR. */
    @PostMapping("/tao-ma")
<<<<<<< Updated upstream
    public ResponseEntity<ApiResponse<TaoMaVnPayResponse>> taoMa(
            @Valid @RequestBody DatHangRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = service.taoMa(request);
        // Dùng IP LAN của máy chủ (không dùng localhost) để điện thoại quét QR truy cập được.
        String host = layIpLan(httpRequest);
        String qrData = "http://" + host + ":" + httpRequest.getServerPort()
                + "/api/v1/client/vnpay/xac-nhan/" + token;
        String maGiaoDich = service.layMaGiaoDich(token);
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo mã thanh toán thành công",
                new TaoMaVnPayResponse(token, qrData, maGiaoDich)
        ));
=======
    public ResponseEntity<ApiResponse<TaoMaVnPayResponse>> taoMa(@Valid @RequestBody DatHangRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo mã thanh toán thành công", service.taoMa(request)));
>>>>>>> Stashed changes
    }

    /** Xác nhận THỦ CÔNG (mở URL) - chỉ để test không cần chuyển khoản thật. */
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
}
