package com.datang.aibase.mcpgateway.entity;
import com.datang.aibase.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class McpClientConn extends BaseEntity {
    private String serverId;
    private String status = "DISCONNECTED";
    private String sessionToken;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
    private Integer errorCount = 0;
    private String lastError;

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }
    public LocalDateTime getDisconnectedAt() { return disconnectedAt; }
    public void setDisconnectedAt(LocalDateTime disconnectedAt) { this.disconnectedAt = disconnectedAt; }
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
