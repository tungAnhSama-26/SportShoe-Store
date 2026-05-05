package com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.request.ChatLieuGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.response.ChatLieuGiayResponse;
import com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.service.ChatLieuGiayService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/danh-muc/chat-lieu-giay")
public class ChatLieuGiayController {

    private final ChatLieuGiayService chatLieuGiayService;

    public ChatLieuGiayController(ChatLieuGiayService chatLieuGiayService) {
        this.chatLieuGiayService = chatLieuGiayService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ChatLieuGiayResponse>>> danhSachChatLieuGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách chất liệu giày thành công",
                chatLieuGiayService.danhSachChatLieuGiay(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> chiTietChatLieuGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết chất liệu giày thành công",
                chatLieuGiayService.chiTietChatLieuGiay(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> taoChatLieuGiay(@Valid @RequestBody ChatLieuGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo chất liệu giày thành công",
                chatLieuGiayService.taoChatLieuGiay(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChatLieuGiayResponse>> capNhatChatLieuGiay(
            @PathVariable Integer id,
            @Valid @RequestBody ChatLieuGiayRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật chất liệu giày thành công",
                chatLieuGiayService.capNhatChatLieuGiay(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiChatLieuGiay(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        chatLieuGiayService.doiTrangThaiChatLieuGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaChatLieuGiay(@PathVariable Integer id) {
        chatLieuGiayService.xoaChatLieuGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chất liệu giày thành công", null));
    }
}
