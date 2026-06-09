package com.datang.aibase.workflow.entity;

import com.datang.aibase.common.entity.BaseEntity;

import java.time.LocalDateTime;

public class WfNodeExec extends BaseEntity {
    private String wfExecId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String status = "PENDING";
    private String input;
    private String output;
    private String error;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public String getWfExecId() { return wfExecId; }
    public void setWfExecId(String wfExecId) { this.wfExecId = wfExecId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
}
