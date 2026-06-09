package com.datang.aibase.api.dto;

public class KbConfigInfo {
    private String id;
    private String name;
    private String description;
    private String kbType;
    private String ownerId;
    private String ownerDeptId;
    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Integer documentCount;
    private String status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getKbType() { return kbType; }
    public void setKbType(String kbType) { this.kbType = kbType; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerDeptId() { return ownerDeptId; }
    public void setOwnerDeptId(String ownerDeptId) { this.ownerDeptId = ownerDeptId; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }
    public Integer getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(Integer chunkOverlap) { this.chunkOverlap = chunkOverlap; }
    public Integer getDocumentCount() { return documentCount; }
    public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
