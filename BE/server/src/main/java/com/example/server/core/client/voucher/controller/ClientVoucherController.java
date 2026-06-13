package com.example.server.core.client.voucher.controller;

import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.voucher.dto.KiemTraVoucherRequest;
import com.example.server.core.client.voucher.dto.VoucherKhaDungResponse;
import com.example.server.core.client.voucher.dto.VoucherResponse;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API kiểm tra/áp mã giảm giá phía khách hàng (trên giỏ hàng hiện tại).
 */
@RestController
@RequestMapping("/api/v1/client/voucher")
public class ClientVoucherController {

    private final ClientVoucherService voucherService;
    private final ClientCheckoutItemService checkoutItemService;

    public ClientVoucherController(
            ClientVoucherService voucherService,
            ClientCheckoutItemService checkoutItemService
    ) {
        this.voucherService = voucherService;
        this.checkoutItemService = checkoutItemService;
    }

    @PostMapping("/kiem-tra")
    public ResponseEntity<ApiResponse<VoucherResponse>> kiemTra(@Valid @RequestBody KiemTraVoucherRequest request) {
        BigDecimal tong = checkoutItemService.chuanBi(request.sanPhams()).tongTienHang();
        return ResponseEntity.ok(ApiResponse.success(
                "Áp mã giảm giá thành công",
                voucherService.kiemTra(request.khachHangId(), request.maPhieu(), tong)
        ));
    }

    /** Danh sách voucher khách có thể dùng cho giỏ hiện tại (toàn sàn + voucher riêng được gửi). */
    @GetMapping("/kha-dung")
    public ResponseEntity<ApiResponse<List<VoucherKhaDungResponse>>> khaDung(
            @RequestParam UUID khachHangId,
            @RequestParam(defaultValue = "0") BigDecimal tongTienHang
    ) {
        BigDecimal tong = tongTienHang.max(BigDecimal.ZERO);
        return ResponseEntity.ok(ApiResponse.success(
                "Danh sách voucher khả dụng",
                voucherService.layVoucherKhaDung(khachHangId, tong)
        ));
    }
}
