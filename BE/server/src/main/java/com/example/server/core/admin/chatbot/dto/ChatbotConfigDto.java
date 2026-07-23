package com.example.server.core.admin.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotConfigDto {
    private String openaiApiKey;
    private String geminiApiKey;
    private String deepseekApiKey;
    private String groqApiKey;
}
