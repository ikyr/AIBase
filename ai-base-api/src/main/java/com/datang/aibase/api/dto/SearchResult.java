package com.datang.aibase.api.dto;

import java.util.List;

public class SearchResult {
    private String query;
    private List<SearchHit> hits;
    private long tookMs;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<SearchHit> getHits() { return hits; }
    public void setHits(List<SearchHit> hits) { this.hits = hits; }

    public long getTookMs() { return tookMs; }
    public void setTookMs(long tookMs) { this.tookMs = tookMs; }

    public static class SearchHit {
        private String chunkId;
        private String docId;
        private String content;
        private double score;
        private Object metadata;

        public String getChunkId() { return chunkId; }
        public void setChunkId(String chunkId) { this.chunkId = chunkId; }

        public String getDocId() { return docId; }
        public void setDocId(String docId) { this.docId = docId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public Object getMetadata() { return metadata; }
        public void setMetadata(Object metadata) { this.metadata = metadata; }
    }
}
