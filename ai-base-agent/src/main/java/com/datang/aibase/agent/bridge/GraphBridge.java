package com.datang.aibase.agent.bridge;

import com.datang.aibase.agent.client.WorkflowServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GraphBridge {

    private static final Logger log = LoggerFactory.getLogger(GraphBridge.class);

    private final WorkflowServiceClient workflowClient;

    public GraphBridge(WorkflowServiceClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public Map<String, Object> submit(String workflowDefinitionId, Map<String, Object> input) {
        log.info("GraphBridge submitting workflow {} with input keys: {}", workflowDefinitionId, input.keySet());

        Map<String, Object> instance = workflowClient.startWorkflow(workflowDefinitionId, input);
        if (instance == null || instance.containsKey("error")) {
            return Map.of("error", "Failed to start workflow",
                    "detail", instance != null ? instance.get("error") : "null response");
        }

        String instanceId = String.valueOf(instance.getOrDefault("id", ""));
        String status = String.valueOf(instance.getOrDefault("status", "UNKNOWN"));

        log.info("GraphBridge workflow instance {} started, initial status: {}", instanceId, status);

        int maxPolls = 120;
        int pollIntervalMs = 2000;

        for (int i = 0; i < maxPolls; i++) {
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Map<String, Object> current = workflowClient.getInstance(instanceId);
            if (current == null) continue;

            String currentStatus = String.valueOf(current.getOrDefault("status", ""));
            if ("COMPLETED".equals(currentStatus) || "FAILED".equals(currentStatus)) {
                log.info("GraphBridge workflow {} finished with status: {}", instanceId, currentStatus);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("instanceId", instanceId);
                result.put("status", currentStatus);
                result.put("output", current.get("output"));
                if (current.containsKey("errorMsg")) {
                    result.put("error", current.get("errorMsg"));
                }
                return result;
            }

            if (i % 5 == 0) {
                log.debug("GraphBridge polling workflow {} status: {} (attempt {}/{})",
                        instanceId, currentStatus, i + 1, maxPolls);
            }
        }

        return Map.of("error", "Workflow timed out after " + (maxPolls * pollIntervalMs / 1000) + "s",
                "instanceId", instanceId);
    }
}
