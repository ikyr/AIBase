package com.datang.aibase.eval.entity;
import com.datang.aibase.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class EvalTask extends BaseEntity {
    private String datasetId;
    private String targetId;
    private String targetType;
    private String status = "PENDING";
    private String metrics;
    private Integer totalItems;
    private Integer passedItems;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String traceId;

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    public Integer getPassedItems() { return passedItems; }
    public void setPassedItems(Integer passedItems) { this.passedItems = passedItems; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
