package com.datang.aibase.knowledge.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DashScopeSearchAdapter implements SearchEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(DashScopeSearchAdapter.class);

    private final RestClient restClient;

    public DashScopeSearchAdapter(@Value("${dashscope.search.url:https://dashscope.aliyuncs.com/api/v1/services/search}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public String getName() {
        return "dashscope";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SearchResult> search(String query, int maxResults) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DASHSCOPE_API_KEY not set, DashScope search unavailable");
            return List.of();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        body.put("top_n", maxResults);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/text2text/generative-search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.warn("Empty response from DashScope search");
                return List.of();
            }

            Object output = response.get("output");
            if (!(output instanceof Map<?, ?> outputMap)) {
                return List.of();
            }

            Object docs = outputMap.get("documents");
            if (!(docs instanceof List<?> docList)) {
                return List.of();
            }

            List<SearchResult> results = new ArrayList<>();
            for (Object doc : docList) {
                if (doc instanceof Map<?, ?> docMap) {
                    Map<String, Object> dm = (Map<String, Object>) docMap;
                    String title = String.valueOf(dm.getOrDefault("title", ""));
                    String url = String.valueOf(dm.getOrDefault("url", ""));
                    String text = String.valueOf(dm.getOrDefault("text", ""));
                    Map<String, String> meta = new LinkedHashMap<>();
                    meta.put("source", "dashscope");
                    meta.put("title", title);
                    meta.put("url", url);
                    results.add(new SearchResult(title, url, text, meta));
                }
            }
            log.info("DashScope search returned {} results for '{}'", results.size(), query);
            return results;
        } catch (Exception e) {
            log.error("DashScope search failed: {}", e.getMessage());
            return List.of();
        }
    }
}
