package com.datang.aibase.platform.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class ApprovalRecord extends BaseEntity {
    private String type;
    private String refType;
    private String refId;
    private String refName;
    private String requester;
    private String approvers;
    private String status = "PENDING";
    private String reason;
    private Integer chainStep;
    private Integer totalSteps;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public String getRefName() { return refName; }
    public void setRefName(String refName) { this.refName = refName; }
    public String getRequester() { return requester; }
    public void setRequester(String requester) { this.requester = requester; }
    public String getApprovers() { return approvers; }
    public void setApprovers(String approvers) { this.approvers = approvers; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getChainStep() { return chainStep; }
    public void setChainStep(Integer chainStep) { this.chainStep = chainStep; }
    public Integer getTotalSteps() { return totalSteps; }
    public void setTotalSteps(Integer totalSteps) { this.totalSteps = totalSteps; }
}
