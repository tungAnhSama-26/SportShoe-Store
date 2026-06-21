package com.example.server.core.admin.quanlychat.controller;

import com.example.server.core.client.chatbot.dto.ChatbotMessageDto;
import com.example.server.core.client.chatbot.dto.ChatbotSessionDto;
import com.example.server.core.client.chatbot.service.ChatbotService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/chat")
public class AdminChatController {

    private final ChatbotService chatbotService;

    public AdminChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public record AdminChatReplyRequest(String message) {}

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatbotSessionDto>>> getActiveSessions() {
        List<ChatbotSessionDto> sessions = chatbotService.getActiveSessions();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiên chat thành công", sessions));
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatbotMessageDto>>> getMessages(@PathVariable Integer id) {
        List<ChatbotMessageDto> messages = chatbotService.getMessagesBySession(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tin nhắn thành công", messages));
    }

    @PostMapping("/sessions/{id}/reply")
    public ResponseEntity<ApiResponse<Void>> reply(@PathVariable Integer id, @RequestBody AdminChatReplyRequest request) {
        chatbotService.replyFromStaff(id, request.message());
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", null));
    }

    @PostMapping("/sessions/{id}/close")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Integer id) {
        chatbotService.closeSession(id);
        return ResponseEntity.ok(ApiResponse.success("Đóng cuộc hội thoại thành công", null));
    }
}
