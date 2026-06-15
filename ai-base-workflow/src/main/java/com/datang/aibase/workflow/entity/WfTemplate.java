package com.datang.aibase.workflow.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class WfTemplate extends BaseEntity {
    private String name;
    private String description;
    private String category;
    private String dag;
    private Integer usageCount = 0;
    private String status = "PUBLISHED";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDag() { return dag; }
    public void setDag(String dag) { this.dag = dag; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
