package com.datang.aibase.skill.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class SkillVersion extends BaseEntity {
    private String skillId;
    private String version;
    private String changelog;
    private String definition;
    private Boolean isLatest = false;
    private String status = "ACTIVE";

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getChangelog() { return changelog; }
    public void setChangelog(String changelog) { this.changelog = changelog; }
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
