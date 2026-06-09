package com.datang.aibase.mcp.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class McpTool extends BaseEntity {
    private String serverId;
    private String name;
    private String description;
    private String inputSchema;
    private String status = "ACTIVE";

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
