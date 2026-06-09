package com.datang.aibase.model.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class ModelRouteRule extends BaseEntity {
    private String name;
    private String modelId;
    private String matchExpression;
    private Integer priority = 0;
    private String fallbackModelId;
    private String status = "ACTIVE";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getMatchExpression() { return matchExpression; }
    public void setMatchExpression(String matchExpression) { this.matchExpression = matchExpression; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getFallbackModelId() { return fallbackModelId; }
    public void setFallbackModelId(String fallbackModelId) { this.fallbackModelId = fallbackModelId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
