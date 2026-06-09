package com.datang.aibase.skill.model;

public class SkillInfo {
    private String id;
    private String name;
    private String description;
    private String skillLevel;
    private String status;

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
}
