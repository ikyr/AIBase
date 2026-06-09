package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class HttpRequestTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestTool.class);

    @Override
    public String getName() { return "http_request"; }

    @Override
    public String getDescription() { return "Make an HTTP GET request to a URL. Input: url (string)."; }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"url\":{\"type\":\"string\"}},\"required\":[\"url\"]}";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        String url = (String) input.get("url");
        if (url == null || url.isBlank()) {
            return Map.of("error", "No URL provided");
        }
        try {
            RestClient client = RestClient.create();
            String result = client.get().uri(url).retrieve().body(String.class);
            return Map.of("url", url, "status", 200, "body", result != null ? result : "");
        } catch (Exception e) {
            log.warn("HTTP request failed for '{}': {}", url, e.getMessage());
            return Map.of("error", "HTTP request failed: " + e.getMessage());
        }
    }
}
