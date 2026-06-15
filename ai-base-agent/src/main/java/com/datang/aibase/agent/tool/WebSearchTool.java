package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class WebSearchTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);
    private final RestClient knowledgeClient;

    public WebSearchTool(
            @Value("${knowledge.service.url:http://localhost:8101}") String knowledgeUrl) {
        this.knowledgeClient = RestClient.create(knowledgeUrl);
    }

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Search the web for real-time information. Input: {\"query\": \"your search query\", \"max_results\": 5}";
    }

    @Override
    public String getInputSchema() {
        return """
        {
          "type": "object",
          "properties": {
            "query": {"type": "string", "description": "The search query"},
            "max_results": {"type": "integer", "description": "Max results to return", "default": 5}
          },
          "required": ["query"]
        }""";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        String query = input.get("query") != null ? input.get("query").toString() : "";
        int maxResults = input.get("max_results") instanceof Number n ? n.intValue() : 5;

        if (query.isBlank()) {
            return Map.of("error", "query is required");
        }

        try {
            Map<String, Object> body = Map.of("query", query, "maxResults", maxResults);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = knowledgeClient.post()
                    .uri("/api/v1/knowledge/external-search")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Map.of("error", "No response from search service");
            }
            return response;
        } catch (Exception e) {
            log.error("WebSearchTool failed: {}", e.getMessage());
            return Map.of("error", "Web search failed: " + e.getMessage());
        }
    }
}
