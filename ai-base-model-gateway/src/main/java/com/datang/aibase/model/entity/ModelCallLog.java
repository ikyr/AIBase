package com.datang.aibase.model.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class ModelCallLog extends BaseEntity {
    private String modelName;
    private String callerRef;
    private Integer inputTokens;
    private Integer outputTokens;
    private Long durationMs;
    private String cost;
    private String callStatus;
    private String errorMsg;

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getCallerRef() { return callerRef; }
    public void setCallerRef(String callerRef) { this.callerRef = callerRef; }
    public Integer getInputTokens() { return inputTokens; }
    public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
    public Integer getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }
    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
