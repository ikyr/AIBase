package com.datang.aibase.workflow.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ModelGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(ModelGatewayClient.class);
    private final RestClient restClient;

    public ModelGatewayClient(@Value("${model-gateway.url:http://localhost:8104}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @SuppressWarnings("unchecked")
    public String chat(String prompt, String model) {
        Map<String, Object> body = Map.of(
                "prompt", prompt,
                "model", model != null ? model : "qwen-plus"
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/model/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                Object content = data.get("content");
                return content != null ? content.toString() : "";
            }
            return "";
        } catch (Exception e) {
            log.error("Model gateway call failed: {}", e.getMessage());
            return "[Model call failed: " + e.getMessage() + "]";
        }
    }
}
