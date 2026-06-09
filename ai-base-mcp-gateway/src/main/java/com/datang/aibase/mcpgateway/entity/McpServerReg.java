package com.datang.aibase.mcpgateway.entity;
import com.datang.aibase.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class McpServerReg extends BaseEntity {
    private String name;
    private String serverType;
    private String transport;
    private String endpoint;
    private String authConfig;
    private Integer toolsCount = 0;
    private Integer resourcesCount = 0;
    private Integer promptsCount = 0;
    private String healthStatus = "UNKNOWN";
    private LocalDateTime lastHealthCheck;
    private String status = "ACTIVE";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServerType() { return serverType; }
    public void setServerType(String serverType) { this.serverType = serverType; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAuthConfig() { return authConfig; }
    public void setAuthConfig(String authConfig) { this.authConfig = authConfig; }
    public Integer getToolsCount() { return toolsCount; }
    public void setToolsCount(Integer toolsCount) { this.toolsCount = toolsCount; }
    public Integer getResourcesCount() { return resourcesCount; }
    public void setResourcesCount(Integer resourcesCount) { this.resourcesCount = resourcesCount; }
    public Integer getPromptsCount() { return promptsCount; }
    public void setPromptsCount(Integer promptsCount) { this.promptsCount = promptsCount; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
