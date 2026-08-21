package com.example.server.core.admin.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotConfigDto {
    private String openaiApiKey;
    private String geminiApiKey;
    private String deepseekApiKey;
    private String groqApiKey;
    private Boolean localFallbackEnabled;
    private Boolean ollamaReachable;
    private Boolean ollamaModelAvailable;
    private String ollamaModel;
    private String ollamaMessage;
    private List<String> providerOrder;
}
