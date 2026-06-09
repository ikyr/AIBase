package com.datang.aibase.api.dto;

public class SearchRequest {
    private String kbId;
    private String query;
    private int topK = 10;
    private double threshold = 0.7;
    private String searchType = "HYBRID"; // VECTOR, KEYWORD, HYBRID

    public String getKbId() { return kbId; }
    public void setKbId(String kbId) { this.kbId = kbId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
}
