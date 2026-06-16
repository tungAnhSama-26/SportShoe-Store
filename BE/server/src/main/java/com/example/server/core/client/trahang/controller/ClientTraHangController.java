package com.example.server.core.client.trahang.controller;

import com.example.server.core.client.trahang.dto.ClientYeuCauTraHangRequest;
import com.example.server.core.client.trahang.service.ClientTraHangService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/client/tra-hang")
public class ClientTraHangController {

    private final ClientTraHangService service;

    public ClientTraHangController(ClientTraHangService service) {
        this.service = service;
    }

    @PostMapping("/yeu-cau")
    public ResponseEntity<ApiResponse<Void>> yeuCauTraHang(
            @Valid @RequestBody ClientYeuCauTraHangRequest request,
            Authentication authentication
    ) {
        service.yeuCauTraHang(request, currentCustomer(authentication).id());
        return ResponseEntity.ok(ApiResponse.success("Gửi yêu cầu trả hàng/hoàn tiền thành công", null));
    }

    private CustomerPrincipal currentCustomer(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomerPrincipal principal) {
            return principal;
        }
        throw new BusinessException("Vui lòng đăng nhập tài khoản khách hàng");
    }
}
