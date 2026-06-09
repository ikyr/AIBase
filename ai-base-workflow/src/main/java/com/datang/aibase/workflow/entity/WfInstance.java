package com.datang.aibase.workflow.entity;
import com.datang.aibase.common.entity.BaseEntity;
import java.time.LocalDateTime;

public class WfInstance extends BaseEntity {
    private String definitionId;
    private Integer definitionVersion;
    private String status = "RUNNING";
    private String input;
    private String output;
    private String context;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMsg;
    private String traceId;

    public String getDefinitionId() { return definitionId; }
    public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }
    public Integer getDefinitionVersion() { return definitionVersion; }
    public void setDefinitionVersion(Integer definitionVersion) { this.definitionVersion = definitionVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
