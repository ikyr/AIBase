package com.datang.aibase.mcpgateway.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class McpToolReg extends BaseEntity {
    private String serverId;
    private String toolName;
    private String description;
    private String inputSchema;
    private String toolType;
    private String sourceService;
    private Integer cacheTtlSeconds;
    private String status = "ACTIVE";

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public String getToolType() { return toolType; }
    public void setToolType(String toolType) { this.toolType = toolType; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public Integer getCacheTtlSeconds() { return cacheTtlSeconds; }
    public void setCacheTtlSeconds(Integer cacheTtlSeconds) { this.cacheTtlSeconds = cacheTtlSeconds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
