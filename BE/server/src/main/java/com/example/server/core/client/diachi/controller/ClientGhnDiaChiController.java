package com.example.server.core.client.diachi.controller;

import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService.GhnDiaGioi;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService.GhnXa;
import com.example.server.infrastructure.api.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API địa giới GHN (tỉnh -&gt; huyện -&gt; xã) cho dropdown chọn địa chỉ ở trang thanh toán.
 * Dữ liệu lấy trực tiếp từ GHN (đã cache trong GhnShippingService).
 */
@RestController
@RequestMapping("/api/v1/client/ghn")
public class ClientGhnDiaChiController {

    private final GhnShippingService ghnShippingService;

    public ClientGhnDiaChiController(GhnShippingService ghnShippingService) {
        this.ghnShippingService = ghnShippingService;
    }

    @GetMapping("/tinh")
    public ResponseEntity<ApiResponse<List<GhnDiaGioi>>> dsTinh() {
        return ResponseEntity.ok(ApiResponse.success("OK", ghnShippingService.layDanhSachTinh()));
    }

    @GetMapping("/huyen")
    public ResponseEntity<ApiResponse<List<GhnDiaGioi>>> dsHuyen(@RequestParam Integer tinhId) {
        return ResponseEntity.ok(ApiResponse.success("OK", ghnShippingService.layDanhSachHuyen(tinhId)));
    }

    @GetMapping("/xa")
    public ResponseEntity<ApiResponse<List<GhnXa>>> dsXa(@RequestParam Integer huyenId) {
        return ResponseEntity.ok(ApiResponse.success("OK", ghnShippingService.layDanhSachXa(huyenId)));
    }
}
