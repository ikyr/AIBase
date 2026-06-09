package com.datang.aibase.platform.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class PromptVersion extends BaseEntity {
    private String refType;
    private String refId;
    private Integer version = 1;
    private String content;
    private String changelog;
    private Boolean isCurrent = false;
    private String status = "DRAFT";

    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getChangelog() { return changelog; }
    public void setChangelog(String changelog) { this.changelog = changelog; }
    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
