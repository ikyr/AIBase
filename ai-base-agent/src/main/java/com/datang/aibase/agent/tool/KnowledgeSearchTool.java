package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeSearchTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeSearchTool.class);

    private final RestClient knowledgeClient;

    public KnowledgeSearchTool(@Value("${knowledge.url:http://localhost:8101}") String baseUrl) {
        this.knowledgeClient = RestClient.create(baseUrl);
    }

    @Override
    public String getName() { return "knowledge_search"; }

    @Override
    public String getDescription() { return "Search a knowledge base for relevant information. Input: kbId (knowledge base ID), query (search query), topK (optional, default 5)."; }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"kbId\":{\"type\":\"string\"},\"query\":{\"type\":\"string\"},\"topK\":{\"type\":\"integer\"}},\"required\":[\"kbId\",\"query\"]}";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        String kbId = (String) input.get("kbId");
        String query = (String) input.get("query");
        int topK = input.containsKey("topK") ? ((Number) input.get("topK")).intValue() : 5;

        Map<String, Object> body = Map.of("kbId", kbId, "query", query, "topK", topK, "searchType", "HYBRID");

        try {
            Map<String, Object> response = knowledgeClient.post()
                    .uri("/api/v1/knowledge/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Map.of("results", List.of());

            Object data = response.get("data");
            if (data instanceof List<?> list) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("results", list);
                result.put("count", list.size());
                return result;
            }
            return Map.of("results", List.of());
        } catch (Exception e) {
            log.error("Knowledge search failed: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
