package com.datang.aibase.knowledge.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApiConnector implements DataSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ApiConnector.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getName() {
        return "api";
    }

    @Override
    public String getType() {
        return "API";
    }

    @Override
    public boolean testConnection(Map<String, String> config) {
        String baseUrl = config.get("base_url");
        if (baseUrl == null) return false;
        try {
            RestClient client = buildClient(config);
            String response = client.get().uri(baseUrl).retrieve().body(String.class);
            return response != null;
        } catch (Exception e) {
            log.warn("API connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void sync(String kbId, Map<String, String> config, SyncCallback callback) {
        String baseUrl = config.get("base_url");
        String itemsPath = config.get("items_path");
        String titleField = config.getOrDefault("title_field", "title");
        String contentField = config.getOrDefault("content_field", "content");

        if (baseUrl == null) {
            callback.onError("API base_url not configured");
            return;
        }

        try {
            RestClient client = buildClient(config);
            String response = client.get()
                    .uri(baseUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                callback.onError("Empty response from API");
                return;
            }

            JsonNode root = mapper.readTree(response);

            List<JsonNode> items = extractItems(root, itemsPath);
            if (items == null) {
                String content = root.has(contentField)
                        ? root.get(contentField).asText()
                        : response;
                String title = root.has(titleField)
                        ? root.get(titleField).asText()
                        : "API Document";
                callback.onDocument(title, content, "text");
                return;
            }

            int count = 0;
            for (JsonNode item : items) {
                String title = item.has(titleField)
                        ? item.get(titleField).asText()
                        : "Item " + (count + 1);
                String content = item.has(contentField)
                        ? item.get(contentField).asText()
                        : item.toString();
                if (!content.isBlank()) {
                    callback.onDocument(title, content, "text");
                    count++;
                }
            }
            log.info("API connector synced {} items for kb {}", count, kbId);
        } catch (Exception e) {
            callback.onError("API sync failed: " + e.getMessage());
        }
    }

    private List<JsonNode> extractItems(JsonNode root, String itemsPath) {
        if (itemsPath == null || itemsPath.isBlank()) {
            if (root.isArray()) {
                List<JsonNode> items = new ArrayList<>();
                root.forEach(items::add);
                return items;
            }
            return null;
        }

        JsonNode current = root;
        for (String segment : itemsPath.split("\\.")) {
            if (current == null) return null;
            if (segment.contains("[")) {
                String fieldName = segment.substring(0, segment.indexOf('['));
                current = current.get(fieldName);
                if (current != null && current.isArray()) {
                    List<JsonNode> items = new ArrayList<>();
                    current.forEach(items::add);
                    return items;
                }
            } else {
                current = current.get(segment);
            }
        }

        if (current != null && current.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            current.forEach(items::add);
            return items;
        }
        return null;
    }

    private RestClient buildClient(Map<String, String> config) {
        RestClient.Builder builder = RestClient.builder();
        String apiKey = config.get("api_key");
        String bearerToken = config.get("bearer_token");
        if (bearerToken != null) {
            builder.defaultHeader("Authorization", "Bearer " + bearerToken);
        } else if (apiKey != null) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }
        return builder.build();
    }
}
