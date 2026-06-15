package com.datang.aibase.knowledge.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class TavilySearchAdapter implements SearchEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(TavilySearchAdapter.class);
    private final RestClient restClient;

    public TavilySearchAdapter(
            @Value("${tavily.search.url:https://api.tavily.com/search}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public String getName() {
        return "tavily";
    }

    @Override
    public List<SearchResult> search(String query, int maxResults) {
        String apiKey = System.getenv("TAVILY_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("TAVILY_API_KEY not set — skipping Tavily search");
            return List.of();
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("api_key", apiKey);
            body.put("query", query);
            body.put("max_results", Math.min(maxResults, 20));
            body.put("include_answer", false);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("results")) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawResults = (List<Map<String, Object>>) response.get("results");
            if (rawResults == null) return List.of();

            List<SearchResult> results = new ArrayList<>();
            for (Map<String, Object> r : rawResults) {
                results.add(new SearchResult(
                    String.valueOf(r.getOrDefault("title", "")),
                    String.valueOf(r.getOrDefault("url", "")),
                    String.valueOf(r.getOrDefault("content", "")),
                    Map.of("source", "tavily")
                ));
            }
            log.info("Tavily search returned {} results for query: {}", results.size(), query);
            return results;
        } catch (Exception e) {
            log.error("Tavily search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
