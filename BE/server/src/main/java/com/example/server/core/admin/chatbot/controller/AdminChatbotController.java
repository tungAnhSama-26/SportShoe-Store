package com.example.server.core.admin.chatbot.controller;

import com.example.server.core.client.chatbot.service.ChatbotService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.core.client.chatbot.dto.ChatbotMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

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
    public ResponseEntity<ApiResponse<AdminChatResponse>> chat(
            @AuthenticationPrincipal AdminPrincipal principal,
            @RequestBody AdminChatRequest request
    ) {
        String reply = chatbotService.generateAdminAiResponse(principal.id(), request.message());
        return ResponseEntity.ok(ApiResponse.success("Thành công", new AdminChatResponse(reply)));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatbotMessageDto>>> getHistory(
            @AuthenticationPrincipal AdminPrincipal principal
    ) {
        List<ChatbotMessageDto> history = chatbotService.getAdminChatHistory(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Thành công", history));
    }

    @GetMapping("/download-csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadCsv(
            @org.springframework.web.bind.annotation.RequestParam("token") String token
    ) {
        byte[] fileBytes = chatbotService.getExportedFile(token);
        if (fileBytes == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-admin.csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(fileBytes);
    }
}
