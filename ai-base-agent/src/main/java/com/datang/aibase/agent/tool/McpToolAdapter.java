package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class McpToolAdapter implements Tool {

    private static final Logger log = LoggerFactory.getLogger(McpToolAdapter.class);

    private final RestClient mcpClient;

    public McpToolAdapter(@Value("${mcp-gateway.url:http://localhost:8106}") String baseUrl) {
        this.mcpClient = RestClient.create(baseUrl);
    }

    @Override
    public String getName() { return "mcp_tool"; }

    @Override
    public String getDescription() { return "Invoke an MCP tool by ID with parameters. Input: toolId (string), params (object with tool parameters)."; }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"toolId\":{\"type\":\"string\"},\"params\":{\"type\":\"object\"}},\"required\":[\"toolId\"]}";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        String toolId = (String) input.get("toolId");
        Object params = input.getOrDefault("params", Map.of());

        try {
            Map<String, Object> response = mcpClient.post()
                    .uri("/api/v1/mcp/tools/{toolId}/invoke", toolId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Map.of("error", "Empty response from MCP gateway");

            Object data = response.get("data");
            if (data instanceof Map<?, ?> dm) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("toolId", toolId);
                result.put("output", dm);
                return result;
            }
            return Map.of("output", response.toString());
        } catch (Exception e) {
            log.error("MCP tool invocation failed for {}: {}", toolId, e.getMessage());
            return Map.of("error", "MCP tool invocation failed: " + e.getMessage());
        }
    }
}
