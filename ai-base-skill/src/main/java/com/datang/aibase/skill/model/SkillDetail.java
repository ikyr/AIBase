package com.datang.aibase.skill.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class SkillDetail {
    private String id;
    private String name;
    private String description;
    private String skillLevel;
    private String status;
    private String promptTemplate;
    private List<SchemaField> inputSchema;
    private List<SchemaField> outputSchema;
    private List<AgentRef> usedByAgents;
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPromptTemplate() { return promptTemplate; }
    public void setPromptTemplate(String promptTemplate) { this.promptTemplate = promptTemplate; }

    public List<SchemaField> getInputSchema() { return inputSchema; }
    public void setInputSchema(List<SchemaField> inputSchema) { this.inputSchema = inputSchema; }

    public List<SchemaField> getOutputSchema() { return outputSchema; }
    public void setOutputSchema(List<SchemaField> outputSchema) { this.outputSchema = outputSchema; }

    public List<AgentRef> getUsedByAgents() { return usedByAgents; }
    public void setUsedByAgents(List<AgentRef> usedByAgents) { this.usedByAgents = usedByAgents; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchemaField {
        private String field;
        private String type;
        private Boolean required;
        private String description;

        public SchemaField() {}
        public SchemaField(String field, String type, Boolean required, String description) {
            this.field = field; this.type = type; this.required = required; this.description = description;
        }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AgentRef {
        private String id;
        private String name;

        public AgentRef() {}
        public AgentRef(String id, String name) {
            this.id = id; this.name = name;
        }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
