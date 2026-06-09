package com.datang.aibase.skill.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class SkillDef extends BaseEntity {
    private String name;
    private String description;
    private String tags;
    private String skillLevel;
    private String promptTemplate;
    private String params;
    private String inputSchema;
    private String outputSchema;
    private String executionMode;
    private Integer timeoutMs;
    private String agentRefId;
    private String status = "ACTIVE";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public String getPromptTemplate() { return promptTemplate; }
    public void setPromptTemplate(String promptTemplate) { this.promptTemplate = promptTemplate; }
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public String getOutputSchema() { return outputSchema; }
    public void setOutputSchema(String outputSchema) { this.outputSchema = outputSchema; }
    public String getExecutionMode() { return executionMode; }
    public void setExecutionMode(String executionMode) { this.executionMode = executionMode; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public String getAgentRefId() { return agentRefId; }
    public void setAgentRefId(String agentRefId) { this.agentRefId = agentRefId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
