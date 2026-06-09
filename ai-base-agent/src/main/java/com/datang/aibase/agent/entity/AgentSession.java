package com.datang.aibase.agent.entity;
import com.datang.aibase.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class AgentSession extends BaseEntity {
    private String agentId;
    private String userId;
    private String title;
    private String status = "ACTIVE";
    private String context;
    private String traceId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
