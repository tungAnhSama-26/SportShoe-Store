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
    private final com.example.server.core.client.chatbot.config.ChatbotModelConfig chatbotModelConfig;

    public AdminChatbotController(ChatbotService chatbotService, com.example.server.core.client.chatbot.config.ChatbotModelConfig chatbotModelConfig) {
        this.chatbotService = chatbotService;
        this.chatbotModelConfig = chatbotModelConfig;
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

    @PostMapping("/close-session")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> closeSession(
            @AuthenticationPrincipal AdminPrincipal principal
    ) {
        chatbotService.closeAdminAiSession(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Đã đóng phiên trò chuyện thành công", null));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.example.server.core.client.chatbot.dto.ChatbotSessionDto>>> getAdminSessions(
            @AuthenticationPrincipal AdminPrincipal principal
    ) {
        List<com.example.server.core.client.chatbot.dto.ChatbotSessionDto> sessions = chatbotService.getAdminAiSessions(principal.id());
        return ResponseEntity.ok(ApiResponse.success("Thành công", sessions));
    }

    @GetMapping("/session/{sessionId}/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatbotMessageDto>>> getSessionMessages(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable("sessionId") Integer sessionId
    ) {
        List<ChatbotMessageDto> history = chatbotService.getAdminAiSessionMessages(principal.id(), sessionId);
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

    public static java.io.File resolveConfigFile() {
        java.io.File file1 = new java.io.File("data/chatbot-keys.json");
        if (file1.exists()) {
            return file1;
        }
        java.io.File file2 = new java.io.File("BE/server/data/chatbot-keys.json");
        if (file2.exists()) {
            return file2;
        }
        java.io.File beDir = new java.io.File("BE/server");
        if (beDir.exists() && beDir.isDirectory()) {
            return file2;
        }
        return file1;
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.example.server.core.admin.chatbot.dto.ChatbotConfigDto>> getConfig() {
        com.example.server.core.admin.chatbot.dto.ChatbotConfigDto dto = new com.example.server.core.admin.chatbot.dto.ChatbotConfigDto("", "", "", "");
        try {
            java.io.File file = resolveConfigFile();
            if (file.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(file);
                if (node.has("openaiApiKey")) dto.setOpenaiApiKey(maskKey(node.get("openaiApiKey").asText()));
                if (node.has("geminiApiKey")) dto.setGeminiApiKey(maskKey(node.get("geminiApiKey").asText()));
                if (node.has("deepseekApiKey")) dto.setDeepseekApiKey(maskKey(node.get("deepseekApiKey").asText()));
                if (node.has("groqApiKey")) dto.setGroqApiKey(maskKey(node.get("groqApiKey").asText()));
            }
        } catch (Exception e) {
            System.err.println("[ADMIN CHATBOT CONFIG] Failed to read chatbot config: " + e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.success("Thành công", dto));
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> saveConfig(
            @RequestBody com.example.server.core.admin.chatbot.dto.ChatbotConfigDto dto
    ) {
        try {
            java.io.File file = resolveConfigFile();
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            com.fasterxml.jackson.databind.node.ObjectNode root = mapper.createObjectNode();
            if (file.exists()) {
                try {
                    root = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(file);
                } catch (Exception e) {
                    // ignore
                }
            }

            updateKey(root, "openaiApiKey", dto.getOpenaiApiKey());
            updateKey(root, "geminiApiKey", dto.getGeminiApiKey());
            updateKey(root, "deepseekApiKey", dto.getDeepseekApiKey());
            updateKey(root, "groqApiKey", dto.getGroqApiKey());

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
            
            // Hot reload models
            chatbotModelConfig.reloadModels();
            
            return ResponseEntity.ok(ApiResponse.success("Lưu cấu hình thành công và đã nạp nóng các Model AI", null));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể lưu cấu hình: " + e.getMessage()));
        }
    }

    private String maskKey(String key) {
        if (key == null || key.isBlank() || key.length() < 10) {
            return key;
        }
        return key.substring(0, 6) + "..." + key.substring(key.length() - 4);
    }

    private void updateKey(com.fasterxml.jackson.databind.node.ObjectNode root, String fieldName, String newKey) {
        if (newKey != null && !newKey.isBlank() && !newKey.contains("...")) {
            root.put(fieldName, newKey.trim());
        }
    }
}
