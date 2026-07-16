package com.example.server.core.admin.chatbot.controller;

import com.example.server.core.client.chatbot.service.ChatbotService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin/chatbot")
public class AdminChatbotController {

    private final ChatbotService chatbotService;

    public AdminChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public record AdminChatRequest(String message) {}
    public record AdminChatResponse(String reply) {}

    @PostMapping("/chat")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminChatResponse>> chat(@RequestBody AdminChatRequest request) {
        String reply = chatbotService.generateAdminAiResponse(request.message());
        return ResponseEntity.ok(ApiResponse.success("Thành công", new AdminChatResponse(reply)));
    }
}
