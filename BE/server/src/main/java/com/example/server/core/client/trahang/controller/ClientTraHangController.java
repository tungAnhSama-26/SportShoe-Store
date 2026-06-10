package com.example.server.core.client.trahang.controller;

import com.example.server.core.client.trahang.dto.ClientYeuCauTraHangRequest;
import com.example.server.core.client.trahang.service.ClientTraHangService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @RequestParam UUID khachHangId
    ) {
        service.yeuCauTraHang(request, khachHangId);
        return ResponseEntity.ok(ApiResponse.success("Gửi yêu cầu trả hàng/hoàn tiền thành công", null));
    }
}
