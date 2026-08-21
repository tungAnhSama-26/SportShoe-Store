package com.example.server.core.client.chatbot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class ChatbotModelConfigTest {

    @Test
    void fallsBackToOllamaWhenCloudProviderFails() {
        ChatModel cloud = mock(ChatModel.class);
        ChatModel ollama = mock(ChatModel.class);
        Prompt prompt = mock(Prompt.class);
        ChatResponse localResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);

        when(cloud.call(prompt)).thenThrow(new RuntimeException("quota exceeded"));
        when(ollama.call(prompt)).thenReturn(localResponse);
        when(localResponse.getResult()).thenReturn(generation);

        var cloudProvider = new ChatbotModelConfig.ProviderChatModel("Cloud", cloud, null, 0);
        var localProvider = new ChatbotModelConfig.ProviderChatModel("Ollama-Local", ollama, null, 0);
        var fallback = new ChatbotModelConfig.FallbackChatModel(List.of(cloudProvider, localProvider));

        assertSame(localResponse, fallback.call(prompt));
        assertEquals(List.of("Cloud", "Ollama-Local"), fallback.getProviderNames());
        verify(cloud).call(prompt);
        verify(ollama).call(prompt);
    }

    @Test
    void failsClearlyWhenNoProviderIsConfigured() {
        var fallback = new ChatbotModelConfig.FallbackChatModel(List.of());

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> fallback.call(mock(Prompt.class)));

        assertEquals("Không có AI model nào được cấu hình.", error.getMessage());
    }

    @Test
    void rejectsLocalRequestWhenConcurrencySlotIsUnavailable() {
        ChatModel ollama = mock(ChatModel.class);
        var localProvider = new ChatbotModelConfig.ProviderChatModel(
                "Ollama-Local", ollama, new Semaphore(0), 0);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> localProvider.call(mock(Prompt.class)));

        assertEquals("AI local đang bận, vui lòng thử lại sau", error.getMessage());
    }

    @Test
    void retryTemplateExecutesFailedProviderOnlyOnce() {
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();

        assertThrows(RuntimeException.class, () ->
                ChatbotModelConfig.singleAttemptRetryTemplate().execute(context -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("provider failed");
                }));

        assertEquals(1, attempts.get());
    }
}
