package com.example.server.core.client.goiy.controller;

import com.example.server.core.client.goiy.dto.GoiYDtos.CauHoiResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYRequest;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYResponse;
import com.example.server.core.client.goiy.service.ClientGoiYService;
import com.example.server.infrastructure.api.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Gợi ý giày bằng AI cho khách: lấy bộ câu hỏi và nhận kết quả gợi ý. */
@RestController
@RequestMapping("/api/v1/client/goi-y")
public class ClientGoiYController {

    private final ClientGoiYService service;

    public ClientGoiYController(ClientGoiYService service) {
        this.service = service;
    }

    /** Bộ câu hỏi trắc nghiệm (mỗi câu chọn được nhiều đáp án). */
    @GetMapping("/cau-hoi")
    public ResponseEntity<ApiResponse<List<CauHoiResponse>>> layCauHoi() {
        return ResponseEntity.ok(ApiResponse.success("Lấy câu hỏi thành công", service.layCauHoi()));
    }

    /** Gửi đáp án (kèm ảnh outfit không bắt buộc) -> AI gợi ý giày phù hợp. */
    @PostMapping
    public ResponseEntity<ApiResponse<GoiYResponse>> goiY(@RequestBody GoiYRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Gợi ý thành công", service.goiY(request)));
    }
}
