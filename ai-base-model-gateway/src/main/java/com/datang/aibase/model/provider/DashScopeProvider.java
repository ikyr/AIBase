package com.datang.aibase.model.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DashScopeProvider implements ModelProvider {

    private static final Logger log = LoggerFactory.getLogger(DashScopeProvider.class);
    private static final String API_ENDPOINT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final RestClient restClient = RestClient.create();

    @Override
    public String getProviderName() {
        return "DASHSCOPE";
    }

    @Override
    public Map<String, Object> chat(String endpoint, String apiKey, String model, Map<String, Object> request) {
        String url = endpoint != null && !endpoint.isBlank() ? endpoint : API_ENDPOINT;
        try {
            List<Map<String, String>> messages = buildMessages(request);
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", request.getOrDefault("temperature", 0.7));
            body.put("max_tokens", request.getOrDefault("maxTokens", 2048));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resolveApiKey(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return parseChatResponse(response);
        } catch (Exception e) {
            log.error("DashScope chat failed: {}", e.getMessage());
            return Map.of("error", "DashScope API call failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> embed(String endpoint, String apiKey, String model, String text) {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("input", Map.of("texts", List.of(text)));

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resolveApiKey(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return parseEmbedResponse(response);
        } catch (Exception e) {
            log.error("DashScope embed failed: {}", e.getMessage());
            return Map.of("error", "Embedding failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> buildMessages(Map<String, Object> request) {
        Object msgs = request.get("messages");
        if (msgs instanceof List<?> list) {
            return (List<Map<String, String>>) list;
        }
        String content = String.valueOf(request.getOrDefault("message", ""));
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", content);
        messages.add(msg);
        return messages;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseChatResponse(Map<String, Object> response) {
        if (response == null) return Map.of("error", "Empty response");
        Map<String, Object> result = new HashMap<>();
        result.put("model", response.get("model"));
        Object choices = response.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> choice) {
            Object message = ((Map<String, Object>) choice).get("message");
            if (message instanceof Map<?, ?> msg) {
                result.put("content", ((Map<String, Object>) msg).getOrDefault("content", ""));
                result.put("role", ((Map<String, Object>) msg).getOrDefault("role", "assistant"));
            }
        }
        result.put("usage", response.get("usage"));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEmbedResponse(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();
        Object data = response != null ? response.get("output") : null;
        if (data instanceof Map<?, ?> output) {
            Object embeddings = output.get("embeddings");
            if (embeddings instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> emb) {
                result.put("embedding", ((Map<String, Object>) emb).get("embedding"));
            }
        }
        return result;
    }

    private String resolveApiKey(String apiKey) {
        if (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("$")) return apiKey;
        String fromEnv = "DASHSCOPE_API_KEY";
        String key = System.getenv(fromEnv);
        if (key == null) key = System.getProperty("DASHSCOPE_API_KEY");
        if (key == null) log.warn("DASHSCOPE_API_KEY not set; API calls may fail");
        return key != null ? key : "";
    }
}
