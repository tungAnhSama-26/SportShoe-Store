package com.example.server.core.client.donhang.controller;

import com.example.server.core.client.donhang.dto.CapNhatThongTinGiaoHangRequest;
import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.dto.DonHangTomTatResponse;
import com.example.server.core.client.donhang.service.ClientXemDonHangService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.CustomerPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/client/don-hang")
public class ClientXemDonHangController {

    private final ClientXemDonHangService service;

    public ClientXemDonHangController(ClientXemDonHangService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DonHangTomTatResponse>>> danhSach(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng thành công",
                service.danhSach(currentCustomer(authentication).id())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonHangChiTietResponse>> chiTiet(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đơn hàng thành công",
                service.chiTiet(currentCustomer(authentication).id(), id)
        ));
    }

    @PostMapping("/{id}/da-nhan-hang")
    public ResponseEntity<ApiResponse<Void>> daNhanHang(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        service.xacNhanDaNhanHang(currentCustomer(authentication).id(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận nhận hàng", null));
    }

    @PostMapping("/{id}/yeu-cau-huy")
    public ResponseEntity<ApiResponse<Void>> yeuCauHuy(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        service.yeuCauHuy(currentCustomer(authentication).id(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu hủy đơn hàng", null));
    }

    @PutMapping("/{id}/thong-tin-giao-hang")
    public ResponseEntity<ApiResponse<DonHangChiTietResponse>> capNhatThongTinGiaoHang(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatThongTinGiaoHangRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật thông tin giao hàng thành công",
                service.capNhatThongTinGiaoHang(currentCustomer(authentication).id(), id, request)
        ));
    }

    private CustomerPrincipal currentCustomer(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomerPrincipal principal) {
            return principal;
        }
        throw new BusinessException("Vui lòng đăng nhập tài khoản khách hàng");
    }
}
