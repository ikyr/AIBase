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
public class SkillTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(SkillTool.class);

    private final RestClient skillClient;

    public SkillTool(@Value("${skill.url:http://localhost:8102}") String baseUrl) {
        this.skillClient = RestClient.create(baseUrl);
    }

    @Override
    public String getName() { return "skill_executor"; }

    @Override
    public String getDescription() { return "Execute a skill by ID with input context. Input: skillId (string), context (object with skill parameters)."; }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"skillId\":{\"type\":\"string\"},\"context\":{\"type\":\"object\"}},\"required\":[\"skillId\"]}";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        String skillId = (String) input.get("skillId");
        Object context = input.getOrDefault("context", Map.of());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("skillId", skillId);
        body.put("context", context);

        try {
            Map<String, Object> response = skillClient.post()
                    .uri("/api/v1/skill/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Map.of("error", "Empty response from skill service");

            Object data = response.get("data");
            if (data instanceof Map<?, ?> dm) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("skillId", skillId);
                result.put("output", dm);
                return result;
            }
            return Map.of("output", response.toString());
        } catch (Exception e) {
            log.error("Skill execution failed for {}: {}", skillId, e.getMessage());
            return Map.of("error", "Skill execution failed: " + e.getMessage());
        }
    }
}
