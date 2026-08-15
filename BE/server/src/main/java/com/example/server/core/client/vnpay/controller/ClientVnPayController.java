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
import org.springframework.web.bind.annotation.RequestParam;
import java.util.HashMap;

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
    public ResponseEntity<ApiResponse<TaoMaVnPayResponse>> taoMa(@Valid @RequestBody DatHangRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo mã thanh toán thành công", service.taoMa(request)));
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
                    <button onclick="quayLai()" style="margin-top:24px;width:100%;background:#16a34a;color:#fff;border:none;border-radius:12px;padding:12px 24px;font-size:15px;font-weight:bold;cursor:pointer;transition:background 0.2s;">Quay lại trang chủ</button>
                  </div>
                  <script>
                    function quayLai() {
                      if (window.opener) {
                        try { window.opener.focus(); } catch(e){}
                        window.close();
                      } else {
                        var origin = window.location.origin;
                        var target = origin.includes(':8080') ? origin.replace(':8080', ':3000') : origin;
                        window.location.href = target + "/khachhang/san-pham";
                      }
                    }
                  </script>
                </body></html>
                """.replace("__MA__", maHoaDon == null ? "" : maHoaDon);
        return ResponseEntity.ok(html);
    }

    /** FE poll: trạng thái thanh toán + mã hóa đơn nếu đã tạo. */
    @GetMapping("/trang-thai/{token}")
    public ResponseEntity<ApiResponse<Map<String, String>>> trangThai(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.success("OK", service.trangThai(token)));
    }

    @PostMapping("/huy/{token}")
    public ResponseEntity<ApiResponse<Void>> huy(@PathVariable String token) {
        service.huy(token);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy phiên thanh toán", null));
    }

    /** Callback handler for VNPAY Redirect */
    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam Map<String, String> params) {
        try {
            String maHoaDon = service.xacNhanCallback(params);
            String html = """
                    <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Thanh toán thành công</title></head>
                    <body style="margin:0;font-family:Arial,sans-serif;background:#f4f4f7;display:flex;align-items:center;justify-content:center;height:100vh;">
                      <div style="background:#fff;border-radius:20px;padding:40px 32px;text-align:center;box-shadow:0 12px 30px rgba(0,0,0,.08);max-width:360px;">
                        <div style="width:72px;height:72px;border-radius:999px;background:#dcfce7;color:#16a34a;font-size:40px;line-height:72px;margin:0 auto 16px;">&#10004;</div>
                        <h1 style="font-size:22px;color:#16a34a;margin:0 0 8px;">Thanh toán thành công</h1>
                        <p style="color:#6b7280;font-size:15px;margin:0;">Đơn hàng <b>__MA__</b> đã được thanh toán trực tuyến thành công.<br>Bạn có thể quay lại trang mua sắm.</p>
                        <button onclick="quayLai()" style="margin-top:24px;width:100%;background:#16a34a;color:#fff;border:none;border-radius:12px;padding:12px 24px;font-size:15px;font-weight:bold;cursor:pointer;transition:background 0.2s;">Quay lại trang chủ</button>
                      </div>
                      <script>
                        function quayLai() {
                          if (window.opener) {
                            try { window.opener.focus(); } catch(e){}
                            window.close();
                          } else {
                            var origin = window.location.origin;
                            var target = origin.includes(':8080') ? origin.replace(':8080', ':3000') : origin;
                            window.location.href = target + "/khachhang/san-pham";
                          }
                        }
                      </script>
                    </body></html>
                    """.replace("__MA__", maHoaDon == null ? "" : maHoaDon);
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            String html = """
                    <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Thanh toán thất bại</title></head>
                    <body style="margin:0;font-family:Arial,sans-serif;background:#f4f4f7;display:flex;align-items:center;justify-content:center;height:100vh;">
                      <div style="background:#fff;border-radius:20px;padding:40px 32px;text-align:center;box-shadow:0 12px 30px rgba(0,0,0,.08);max-width:360px;">
                        <div style="width:72px;height:72px;border-radius:999px;background:#fee2e2;color:#dc2626;font-size:40px;line-height:72px;margin:0 auto 16px;">&#10008;</div>
                        <h1 style="font-size:22px;color:#dc2626;margin:0 0 8px;">Thanh toán thất bại</h1>
                        <p style="color:#6b7280;font-size:15px;margin:0;">__ERR__<br>Vui lòng thử lại sau.</p>
                        <button onclick="quayLai()" style="margin-top:24px;width:100%;background:#dc2626;color:#fff;border:none;border-radius:12px;padding:12px 24px;font-size:15px;font-weight:bold;cursor:pointer;transition:background 0.2s;">Quay lại</button>
                      </div>
                      <script>
                        function quayLai() {
                          if (window.opener) {
                            try { window.opener.focus(); } catch(e){}
                            window.close();
                          } else {
                            var origin = window.location.origin;
                            var target = origin.includes(':8080') ? origin.replace(':8080', ':3000') : origin;
                            window.location.href = target + "/khachhang/san-pham";
                          }
                        }
                      </script>
                    </body></html>
                    """.replace("__ERR__", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi xử lý giao dịch");
            return ResponseEntity.ok(html);
        }
    }

    /** IPN (Instant Payment Notification) handler for VNPAY */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(@RequestParam Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            service.xacNhanCallback(params);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (com.example.server.infrastructure.exception.BusinessException e) {
            if (e.getMessage() != null && e.getMessage().contains("đã thanh toán")) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return ResponseEntity.ok(response);
            }
            response.put("RspCode", "01");
            response.put("Message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown Error");
            return ResponseEntity.ok(response);
        }
    }
}
