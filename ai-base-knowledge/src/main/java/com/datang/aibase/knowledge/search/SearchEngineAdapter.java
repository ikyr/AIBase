package com.datang.aibase.knowledge.search;

import java.util.List;
import java.util.Map;

public interface SearchEngineAdapter {

    String getName();

    List<SearchResult> search(String query, int maxResults);

    record SearchResult(String title, String url, String snippet, Map<String, String> metadata) {}
}
