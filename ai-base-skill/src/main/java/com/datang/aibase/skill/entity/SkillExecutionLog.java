package com.datang.aibase.skill.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class SkillExecutionLog extends BaseEntity {
    private String skillId;
    private String skillVersion;
    private String sessionId;
    private String input;
    private String output;
    private String status = "RUNNING";
    private Integer durationMs;
    private String errorMsg;
    private String traceId;

    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public String getSkillVersion() { return skillVersion; }
    public void setSkillVersion(String skillVersion) { this.skillVersion = skillVersion; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
