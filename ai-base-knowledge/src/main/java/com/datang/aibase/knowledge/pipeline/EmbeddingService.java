package com.datang.aibase.knowledge.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final RestClient restClient;

    public EmbeddingService(@Value("${model-gateway.url:http://localhost:8104}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @SuppressWarnings("unchecked")
    public List<List<Float>> embed(List<String> texts) {
        List<List<Float>> result = new ArrayList<>();
        for (String text : texts) {
            List<Float> vec = embedSingle(text);
            if (!vec.isEmpty()) {
                result.add(vec);
            }
        }
        log.info("Embedded {} / {} text chunks", result.size(), texts.size());
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Float> embedSingle(String text) {
        Map<String, Object> requestBody = Map.of("text", text);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/model/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.warn("Empty response from embedding service");
                return List.of();
            }

            boolean success = response.get("success") instanceof Boolean b && b;
            if (!success) {
                log.warn("Embedding failed: {}", response.getOrDefault("errorMsg", "unknown"));
                return List.of();
            }

            Object data = response.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object embedding = ((Map<String, Object>) dataMap).get("embedding");
                if (embedding instanceof List<?> list) {
                    return list.stream()
                            .filter(Number.class::isInstance)
                            .map(n -> ((Number) n).floatValue())
                            .toList();
                }
            }
        } catch (Exception e) {
            log.error("Embedding API call failed: {}", e.getMessage());
        }
        return List.of();
    }
}
