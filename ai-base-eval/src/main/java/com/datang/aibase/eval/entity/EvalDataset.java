package com.datang.aibase.eval.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class EvalDataset extends BaseEntity {
    private String name;
    private String description;
    private String evalType;
    private String status = "ACTIVE";
    private Integer itemCount = 0;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEvalType() { return evalType; }
    public void setEvalType(String evalType) { this.evalType = evalType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
}
