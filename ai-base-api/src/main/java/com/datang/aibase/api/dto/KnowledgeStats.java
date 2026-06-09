package com.datang.aibase.api.dto;
public class KnowledgeStats {
    private String kbId;
    private long documentCount;
    private long chunkCount;
    private long totalTokens;
    private long storageBytes;
    public String getKbId() { return kbId; }
    public void setKbId(String kbId) { this.kbId = kbId; }
    public long getDocumentCount() { return documentCount; }
    public void setDocumentCount(long documentCount) { this.documentCount = documentCount; }
    public long getChunkCount() { return chunkCount; }
    public void setChunkCount(long chunkCount) { this.chunkCount = chunkCount; }
    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long totalTokens) { this.totalTokens = totalTokens; }
    public long getStorageBytes() { return storageBytes; }
    public void setStorageBytes(long storageBytes) { this.storageBytes = storageBytes; }
}
