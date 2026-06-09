package com.datang.aibase.api.dto;
public class IngestResult {
    private String docId;
    private String status;
    private int chunkCount;
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
}
