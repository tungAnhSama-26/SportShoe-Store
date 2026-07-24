package com.example.server.core.client.chatbot.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import org.springframework.web.client.RestClient;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.HttpRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ChatbotModelConfig {

    @Value("${OPENAI_API_KEY:}")
    private String openAiKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiKey;

    @Value("${DEEPSEEK_API_KEY:}")
    private String deepseekKey;

    @Value("${GROQ_API_KEY:}")
    private String groqKey;

    private final java.util.Map<String, String> thoughtSignatures = new java.util.concurrent.ConcurrentHashMap<>();

    @Bean
    public org.springframework.ai.model.function.FunctionCallbackContext springAiFunctionManager() {
        return new org.springframework.ai.model.function.FunctionCallbackContext();
    }

    private boolean isValidKey(String key) {
        return key != null && !key.isBlank() && 
               !key.equalsIgnoreCase("your-api-key-here") && 
               !key.equalsIgnoreCase("your_api_key_here");
    }

    private ChatModel createModel(String apiKey, String baseUrl, String modelName, String providerName,
                                   org.springframework.ai.model.function.FunctionCallbackContext functionCallbackContext) {
        System.out.println("[AI CHATBOT] Khởi tạo AI provider: " + providerName + " (" + modelName + ")");
        
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestInterceptor((HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
                    String requestBody = new String(body, StandardCharsets.UTF_8);
                    
                    // Inject thought_signature if present in thoughtSignatures map
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(requestBody);
                        boolean requestModified = false;
                        com.fasterxml.jackson.databind.JsonNode messages = root.get("messages");
                        if (messages != null && messages.isArray()) {
                            for (com.fasterxml.jackson.databind.JsonNode message : messages) {
                                com.fasterxml.jackson.databind.JsonNode toolCalls = message.get("tool_calls");
                                if (toolCalls != null && toolCalls.isArray()) {
                                    for (com.fasterxml.jackson.databind.JsonNode tc : toolCalls) {
                                        com.fasterxml.jackson.databind.JsonNode idNode = tc.get("id");
                                        if (idNode != null) {
                                            String callId = idNode.asText();
                                            String thoughtSig = thoughtSignatures.get(callId);
                                            if (thoughtSig != null && !tc.has("extra_content")) {
                                                com.fasterxml.jackson.databind.node.ObjectNode tcObject = (com.fasterxml.jackson.databind.node.ObjectNode) tc;
                                                com.fasterxml.jackson.databind.node.ObjectNode extraContentNode = mapper.createObjectNode();
                                                com.fasterxml.jackson.databind.node.ObjectNode googleNode = mapper.createObjectNode();
                                                googleNode.put("thought_signature", thoughtSig);
                                                extraContentNode.set("google", googleNode);
                                                tcObject.set("extra_content", extraContentNode);
                                                requestModified = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (requestModified) {
                            requestBody = mapper.writeValueAsString(root);
                            body = requestBody.getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (Exception e) {
                        System.err.println("[AI CHATBOT WARNING] Failed to parse/modify request body for thought_signature: " + e.getMessage());
                    }

                    System.out.println("[AI CHATBOT REQUEST] URL: " + request.getURI() + " | Body: " + requestBody);
                    ClientHttpResponse response = execution.execute(request, body);
                    byte[] responseBodyBytes = response.getBody().readAllBytes();
                    String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);
                    System.out.println("[AI CHATBOT RESPONSE] Status: " + response.getStatusCode() + " | Body: " + responseBody);
                    
                    if (response.getStatusCode().isError()) {
                        throw new IOException("AI API Error (HTTP " + response.getStatusCode() + "): " + responseBody);
                    }
                    
                    String trimmed = responseBody.trim();
                    if (trimmed.startsWith("[")) {
                        throw new IOException("AI API returned JSON Array instead of Object: " + responseBody);
                    }

                    // Extract and save thought_signature if present in response
                    if (response.getStatusCode().is2xxSuccessful()) {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(responseBody);
                            com.fasterxml.jackson.databind.JsonNode choices = root.get("choices");
                            if (choices != null && choices.isArray()) {
                                for (com.fasterxml.jackson.databind.JsonNode choice : choices) {
                                    com.fasterxml.jackson.databind.JsonNode message = choice.get("message");
                                    if (message != null) {
                                        com.fasterxml.jackson.databind.JsonNode toolCalls = message.get("tool_calls");
                                        if (toolCalls != null && toolCalls.isArray()) {
                                            for (com.fasterxml.jackson.databind.JsonNode tc : toolCalls) {
                                                com.fasterxml.jackson.databind.JsonNode extraContent = tc.get("extra_content");
                                                com.fasterxml.jackson.databind.JsonNode idNode = tc.get("id");
                                                if (extraContent != null && idNode != null) {
                                                    com.fasterxml.jackson.databind.JsonNode google = extraContent.get("google");
                                                    if (google != null) {
                                                        com.fasterxml.jackson.databind.JsonNode tsNode = google.get("thought_signature");
                                                        if (tsNode != null) {
                                                            String callId = idNode.asText();
                                                            String thoughtSig = tsNode.asText();
                                                            thoughtSignatures.put(callId, thoughtSig);
                                                            System.out.println("[AI CHATBOT] Saved thought signature for " + callId);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors for non-matching responses
                        }
                    }
                    
                    // Sửa lỗi finish_reason không chuẩn (ví dụ: function_call_filter: MALFORMED_FUNCTION_CALL của Gemini)
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"finish_reason\"\\s*:\\s*\"([^\"]+)\"");
                    java.util.regex.Matcher matcher = pattern.matcher(responseBody);
                    StringBuffer sb = new StringBuffer();
                    boolean modified = false;
                    while (matcher.find()) {
                        String value = matcher.group(1);
                        if (!value.equals("stop") && !value.equals("function_call") && !value.equals("length") 
                                && !value.equals("content_filter") && !value.equals("tool_call") && !value.equals("tool_calls")) {
                            System.out.println("[AI CHATBOT WARNING] Sửa finish_reason không hợp lệ: " + value + " -> stop");
                            matcher.appendReplacement(sb, "\"finish_reason\":\"stop\"");
                            modified = true;
                        } else {
                            matcher.appendReplacement(sb, matcher.group(0));
                        }
                    }
                    matcher.appendTail(sb);
                    if (modified) {
                        responseBody = sb.toString();
                        responseBodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    }
                    
                    return new ModifiedClientHttpResponse(response, responseBodyBytes);
                });

        OpenAiApi openAiApi = new OpenAiApi(
                baseUrl, 
                apiKey, 
                restClientBuilder, 
                org.springframework.web.reactive.function.client.WebClient.builder()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(modelName)
                .withTemperature(0.7f)
                .build();
        return new CompatibleOpenAiChatModel(openAiApi, options, functionCallbackContext, new org.springframework.retry.support.RetryTemplate());
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    private FallbackChatModel activeFallbackModel;
    private org.springframework.ai.model.function.FunctionCallbackContext context;

    @Bean
    @Primary
    public ChatModel chatModel(org.springframework.ai.model.function.FunctionCallbackContext functionCallbackContext) {
        this.context = functionCallbackContext;
        List<ChatModel> models = loadModelsFromConfig(functionCallbackContext);
        this.activeFallbackModel = new FallbackChatModel(models);
        return this.activeFallbackModel;
    }

    public synchronized void reloadModels() {
        if (this.activeFallbackModel != null && this.context != null) {
            System.out.println("[AI CHATBOT] Reloading AI Models from file config...");
            List<ChatModel> newModels = loadModelsFromConfig(this.context);
            this.activeFallbackModel.updateModels(newModels);
            System.out.println("[AI CHATBOT] AI Models reloaded successfully. Active models count: " + newModels.size());
        }
    }

    private List<ChatModel> loadModelsFromConfig(org.springframework.ai.model.function.FunctionCallbackContext context) {
        String activeOpenAiKey = openAiKey;
        String activeGeminiKey = geminiKey;
        String activeDeepseekKey = deepseekKey;
        String activeGroqKey = groqKey;

        try {
            java.io.File file = new java.io.File("data/chatbot-keys.json");
            if (file.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(file);
                if (node.has("openaiApiKey") && !node.get("openaiApiKey").asText().isBlank()) {
                    activeOpenAiKey = node.get("openaiApiKey").asText();
                }
                if (node.has("geminiApiKey") && !node.get("geminiApiKey").asText().isBlank()) {
                    activeGeminiKey = node.get("geminiApiKey").asText();
                }
                if (node.has("deepseekApiKey") && !node.get("deepseekApiKey").asText().isBlank()) {
                    activeDeepseekKey = node.get("deepseekApiKey").asText();
                }
                if (node.has("groqApiKey") && !node.get("groqApiKey").asText().isBlank()) {
                    activeGroqKey = node.get("groqApiKey").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("[AI CHATBOT WARNING] Không thể đọc chatbot-keys.json: " + e.getMessage());
        }

        List<ChatModel> models = new ArrayList<>();
        if (isValidKey(activeGroqKey)) {
            models.add(createModel(activeGroqKey, "https://api.groq.com/openai", "llama-3.3-70b-versatile", "Groq", context));
        }
        if (isValidKey(activeGeminiKey)) {
            models.add(createModel(activeGeminiKey, "https://generativelanguage.googleapis.com/v1beta/openai/", "gemini-2.0-flash", "Gemini", context));
            models.add(createModel(activeGeminiKey, "https://generativelanguage.googleapis.com/v1beta/openai/", "gemini-2.0-flash-lite", "Gemini-Lite-DuPhong", context));
        }
        if (isValidKey(activeOpenAiKey)) {
            models.add(createModel(activeOpenAiKey, "https://api.openai.com", "gpt-4o-mini", "OpenAI", context));
        }
        if (isValidKey(activeDeepseekKey)) {
            models.add(createModel(activeDeepseekKey, "https://api.deepseek.com", "deepseek-chat", "DeepSeek", context));
        }

        if (models.isEmpty()) {
            System.out.println("[AI CHATBOT] CẢNH BÁO: Chưa cấu hình bất kỳ API Key hợp lệ nào. Sẽ dùng key giả lập.");
            models.add(createModel("dummy-key", "https://api.openai.com", "gpt-4o-mini", "Dummy-OpenAI", context));
        }
        return models;
    }

    public static class FallbackChatModel implements ChatModel {
        private volatile List<ChatModel> models;

        public FallbackChatModel(List<ChatModel> models) {
            this.models = new java.util.concurrent.CopyOnWriteArrayList<>(models);
        }

        public void updateModels(List<ChatModel> newModels) {
            this.models = new java.util.concurrent.CopyOnWriteArrayList<>(newModels);
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            System.out.println("[AI CHATBOT PROMPT DEBUG] Prompt options: " + prompt.getOptions());
            if (models.isEmpty()) {
                throw new IllegalStateException("Không có AI model nào được cấu hình.");
            }
            List<String> errors = new ArrayList<>();
            Throwable lastError = null;
            for (ChatModel model : models) {
                try {
                    Prompt promptToUse = prompt;
                    ChatOptions promptOptions = (ChatOptions) prompt.getOptions();
                    if (promptOptions != null) {
                        org.springframework.ai.model.ModelOptions modelDefaultOptions = model.getDefaultOptions();
                        if (modelDefaultOptions instanceof OpenAiChatOptions && promptOptions instanceof OpenAiChatOptions) {
                            OpenAiChatOptions modelOpts = (OpenAiChatOptions) modelDefaultOptions;
                            OpenAiChatOptions promptOpts = (OpenAiChatOptions) promptOptions;
                            OpenAiChatOptions mergedOpts = OpenAiChatOptions.builder()
                                    .withModel(modelOpts.getModel())
                                    .withTemperature(promptOpts.getTemperature() != null ? promptOpts.getTemperature() : modelOpts.getTemperature())
                                    .withFrequencyPenalty(promptOpts.getFrequencyPenalty() != null ? promptOpts.getFrequencyPenalty() : modelOpts.getFrequencyPenalty())
                                    .withMaxTokens(promptOpts.getMaxTokens() != null ? promptOpts.getMaxTokens() : modelOpts.getMaxTokens())
                                    .withPresencePenalty(promptOpts.getPresencePenalty() != null ? promptOpts.getPresencePenalty() : modelOpts.getPresencePenalty())
                                    .withResponseFormat(promptOpts.getResponseFormat() != null ? promptOpts.getResponseFormat() : modelOpts.getResponseFormat())
                                    .withStop(promptOpts.getStop() != null ? promptOpts.getStop() : modelOpts.getStop())
                                    .withTopP(promptOpts.getTopP() != null ? promptOpts.getTopP() : modelOpts.getTopP())
                                    .withUser(promptOpts.getUser() != null ? promptOpts.getUser() : modelOpts.getUser())
                                    .withFunctionCallbacks(promptOpts.getFunctionCallbacks() != null ? promptOpts.getFunctionCallbacks() : modelOpts.getFunctionCallbacks())
                                    .withFunctions(promptOpts.getFunctions() != null ? promptOpts.getFunctions() : modelOpts.getFunctions())
                                    .build();
                            promptToUse = new Prompt(prompt.getInstructions(), mergedOpts);
                        }
                    }
                    ChatResponse response = model.call(promptToUse);
                    if (response != null && response.getResult() != null) {
                        return response;
                    }
                    errors.add("Model returned empty response/choices");
                } catch (Throwable t) {
                    lastError = t;
                    String errMsg = t.getMessage();
                    if (t.getCause() != null) {
                        errMsg += " (Cause: " + t.getCause().getMessage() + ")";
                    }
                    errors.add("Model " + model.getClass().getSimpleName() + " failed: " + errMsg);
                    System.err.println("[AI FALLBACK] Lỗi khi gọi model: " + errMsg);
                }
            }
            throw new RuntimeException("Tất cả các AI model đều thất bại: " + String.join(" | ", errors), lastError);
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            if (models.isEmpty()) {
                return Flux.error(new IllegalStateException("Không có AI model nào được cấu hình."));
            }
            return models.get(0).stream(prompt);
        }

        @Override
        public ChatOptions getDefaultOptions() {
            if (models != null && !models.isEmpty()) {
                return (ChatOptions) models.get(0).getDefaultOptions();
            }
            return null;
        }
    }

    public static class CompatibleHttpHeaders extends org.springframework.http.HttpHeaders {
        public CompatibleHttpHeaders(org.springframework.http.HttpHeaders delegate) {
            super();
            this.addAll(delegate);
        }

        public boolean containsKey(Object key) {
            if (key instanceof String) {
                return super.containsKey(key);
            }
            return false;
        }
    }

    public static class CompatibleOpenAiChatModel extends OpenAiChatModel {
        public CompatibleOpenAiChatModel(OpenAiApi openAiApi, OpenAiChatOptions options, 
                                          org.springframework.ai.model.function.FunctionCallbackContext functionCallbackContext, 
                                          org.springframework.retry.support.RetryTemplate retryTemplate) {
            super(openAiApi, options, functionCallbackContext, retryTemplate);
        }

        @Override
        protected ResponseEntity<OpenAiApi.ChatCompletion> doChatCompletion(OpenAiApi.ChatCompletionRequest request) {
            ResponseEntity<OpenAiApi.ChatCompletion> response = super.doChatCompletion(request);
            if (response == null) {
                throw new RuntimeException("Phản hồi từ AI API trống (null)");
            }
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Lỗi từ AI API (HTTP " + response.getStatusCode() + "): " + 
                    (response.getBody() == null ? "Không có dữ liệu phản hồi" : response.getBody().toString()));
            }
            return new ResponseEntity<>(
                response.getBody(),
                new CompatibleHttpHeaders(response.getHeaders()),
                response.getStatusCode()
            );
        }

        @Override
        protected reactor.core.publisher.Flux<ResponseEntity<OpenAiApi.ChatCompletion>> doChatCompletionStream(OpenAiApi.ChatCompletionRequest request) {
            return super.doChatCompletionStream(request)
                .map(response -> {
                    if (response == null) {
                        throw new RuntimeException("Phản hồi stream từ AI API trống (null)");
                    }
                    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                        throw new RuntimeException("Lỗi stream từ AI API (HTTP " + response.getStatusCode() + ")");
                    }
                    return new ResponseEntity<>(
                        response.getBody(),
                        new CompatibleHttpHeaders(response.getHeaders()),
                        response.getStatusCode()
                    );
                });
        }
    }

    public static class ModifiedClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final byte[] bodyBytes;

        public ModifiedClientHttpResponse(ClientHttpResponse delegate, byte[] bodyBytes) {
            this.delegate = delegate;
            this.bodyBytes = bodyBytes;
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public java.io.InputStream getBody() throws IOException {
            return new java.io.ByteArrayInputStream(bodyBytes);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}

