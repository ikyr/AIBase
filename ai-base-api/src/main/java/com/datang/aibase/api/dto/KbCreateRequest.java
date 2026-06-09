package com.datang.aibase.api.dto;

public class KbCreateRequest {
    private String name;
    private String description;
    private String kbType;      // PUBLIC / PERSONAL / DEPARTMENT
    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getKbType() { return kbType; }
    public void setKbType(String kbType) { this.kbType = kbType; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }
    public Integer getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(Integer chunkOverlap) { this.chunkOverlap = chunkOverlap; }
}
