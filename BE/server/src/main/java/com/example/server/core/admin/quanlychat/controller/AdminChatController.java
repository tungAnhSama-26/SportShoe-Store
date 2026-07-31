package com.example.server.core.admin.quanlychat.controller;

import com.example.server.core.client.chatbot.dto.ChatbotMessageDto;
import com.example.server.core.client.chatbot.dto.ChatbotSessionDto;
import com.example.server.core.client.chatbot.service.ChatbotService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/chat")
@Validated
public class AdminChatController {

    private final ChatbotService chatbotService;

    public AdminChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public record AdminChatReplyRequest(
            @NotBlank(message = "Nội dung phản hồi không được để trống")
            @Size(max = 2000, message = "Nội dung phản hồi không được vượt quá 2000 ký tự")
            String message
    ) {}

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatbotSessionDto>>> getActiveSessions() {
        List<ChatbotSessionDto> sessions = chatbotService.getActiveSessions();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiên chat thành công", sessions));
    }

    @GetMapping("/sessions/history")
    public ResponseEntity<ApiResponse<List<ChatbotSessionDto>>> getClosedSessions() {
        List<ChatbotSessionDto> sessions = chatbotService.getClosedSessions();
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử phiên chat thành công", sessions));
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatbotMessageDto>>> getMessages(
            @PathVariable @Min(value = 1, message = "ID phiên chat không hợp lệ") Integer id
    ) {
        List<ChatbotMessageDto> messages = chatbotService.getMessagesBySession(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tin nhắn thành công", messages));
    }

    @PostMapping("/sessions/{id}/reply")
    public ResponseEntity<ApiResponse<Void>> reply(
            @PathVariable @Min(value = 1, message = "ID phiên chat không hợp lệ") Integer id,
            @Valid @RequestBody AdminChatReplyRequest request,
            Authentication authentication) {
        UUID staffId = null;
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            staffId = principal.id();
        }
        chatbotService.replyFromStaff(id, request.message(), staffId);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", null));
    }

    @PostMapping("/sessions/{id}/close")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable @Min(value = 1, message = "ID phiên chat không hợp lệ") Integer id
    ) {
        chatbotService.closeSession(id);
        return ResponseEntity.ok(ApiResponse.success("Đóng cuộc hội thoại thành công", null));
    }
}
