package com.example.server.core.client.chatbot.controller;

import com.example.server.core.client.chatbot.dto.ClientChatRequest;
import com.example.server.core.client.chatbot.dto.ClientChatResponse;
import com.example.server.core.client.chatbot.dto.ChatbotMessageDto;
import com.example.server.core.client.chatbot.service.ChatbotService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.server.infrastructure.security.ratelimit.RateLimit;

@RestController
@RequestMapping("/api/v1/client/chatbot")
@Validated
public class ClientChatbotController {

    private final ChatbotService chatbotService;

    public ClientChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    @RateLimit(limit = 6, durationInSeconds = 10)
    public ResponseEntity<ApiResponse<ClientChatResponse>> chat(@Valid @RequestBody ClientChatRequest request) {
        ClientChatResponse response = chatbotService.handleClientMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Thành công", response));
    }

    @PostMapping("/session/{id}/request-staff")
    public ResponseEntity<ApiResponse<Void>> requestStaff(@PathVariable @Min(value = 1, message = "ID phiên chat phải lớn hơn 0") Integer id) {
        chatbotService.requestStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu hỗ trợ từ nhân viên thành công", null));
    }

    @PostMapping("/session/{id}/close-due-to-inactivity")
    public ResponseEntity<ApiResponse<Void>> closeDueToInactivity(@PathVariable @Min(value = 1, message = "ID phiên chat phải lớn hơn 0") Integer id) {
        chatbotService.closeSessionDueToInactivity(id);
        return ResponseEntity.ok(ApiResponse.success("Đóng phiên chat do khách không hoạt động thành công", null));
    }

    @GetMapping("/session/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatbotMessageDto>>> getMessages(@PathVariable @Min(value = 1, message = "ID phiên chat phải lớn hơn 0") Integer id) {
        List<ChatbotMessageDto> messages = chatbotService.getMessagesBySession(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tin nhắn thành công", messages));
    }
}
