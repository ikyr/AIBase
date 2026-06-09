package com.datang.aibase.agent.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class AgentDef extends BaseEntity {
    private String name;
    private String description;
    private String systemPrompt;
    private String model;
    private String tools;
    private String skillIds;
    private String kbIds;
    private String coordinationMode;
    private String constraints;
    private String status = "ACTIVE";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getTools() { return tools; }
    public void setTools(String tools) { this.tools = tools; }
    public String getSkillIds() { return skillIds; }
    public void setSkillIds(String skillIds) { this.skillIds = skillIds; }
    public String getKbIds() { return kbIds; }
    public void setKbIds(String kbIds) { this.kbIds = kbIds; }
    public String getCoordinationMode() { return coordinationMode; }
    public void setCoordinationMode(String coordinationMode) { this.coordinationMode = coordinationMode; }
    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
