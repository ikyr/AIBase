package com.datang.aibase.workflow.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class WfDefinition extends BaseEntity {
    private String name;
    private String description;
    private Integer version = 1;
    private String dag;
    private Integer timeoutSeconds = 300;
    private String retryPolicy;
    private String status = "DRAFT";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getDag() { return dag; }
    public void setDag(String dag) { this.dag = dag; }
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
