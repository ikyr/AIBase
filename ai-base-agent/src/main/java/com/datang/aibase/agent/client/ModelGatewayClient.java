package com.datang.aibase.agent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ModelGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(ModelGatewayClient.class);

    private final RestClient restClient;

    public ModelGatewayClient(@Value("${model-gateway.url:http://localhost:8104}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = Map.of("messages", messages);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/model/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return "Error: Empty response from model";
            }

            boolean success = response.get("success") instanceof Boolean b && b;
            if (!success) {
                String error = (String) response.getOrDefault("errorMsg", "Unknown error");
                log.warn("Model gateway returned error: {}", error);
                return "Error: " + error;
            }

            Object data = response.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object content = ((Map<String, Object>) dataMap).get("content");
                if (content instanceof String s && !s.isBlank()) {
                    return s;
                }
                Object error = ((Map<String, Object>) dataMap).get("error");
                if (error instanceof String s && !s.isBlank()) {
                    return "Error: " + s;
                }
            }

            return "Error: Unexpected response format from model";
        } catch (Exception e) {
            log.error("Model gateway call failed: {}", e.getMessage());
            return "Error calling model: " + e.getMessage();
        }
    }
}
