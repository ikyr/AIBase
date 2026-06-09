package com.datang.aibase.mcp.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class McpServer extends BaseEntity {
    private String name;
    private String serverType;
    private String transport;
    private String endpoint;
    private String description;
    private String healthStatus = "UNKNOWN";
    private String status = "ACTIVE";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServerType() { return serverType; }
    public void setServerType(String serverType) { this.serverType = serverType; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
