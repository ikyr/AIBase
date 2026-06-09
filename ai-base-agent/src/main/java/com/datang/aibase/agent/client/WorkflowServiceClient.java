package com.datang.aibase.agent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WorkflowServiceClient {

    private static final Logger log = LoggerFactory.getLogger(WorkflowServiceClient.class);
    private final RestClient restClient;

    public WorkflowServiceClient(@Value("${workflow-service.url:http://localhost:8103}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> startWorkflow(String definitionId, Map<String, Object> input) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/api/v1/workflow/{id}/start", definitionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input != null ? input : Map.of())
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                return (Map<String, Object>) data;
            }
            return Map.of("error", "Unexpected workflow response");
        } catch (Exception e) {
            log.error("Failed to start workflow {}: {}", definitionId, e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getInstance(String instanceId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/api/v1/workflow/instances/{id}", instanceId)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                return (Map<String, Object>) data;
            }
            return Map.of("status", "UNKNOWN");
        } catch (Exception e) {
            log.debug("Failed to get workflow instance {}: {}", instanceId, e.getMessage());
            return Map.of("status", "UNKNOWN");
        }
    }
}
