package com.datang.aibase.api.dto;
public class WorkflowResult {
    private String instanceId;
    private String status;
    private Object output;
    private String errorMsg;
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Object getOutput() { return output; }
    public void setOutput(Object output) { this.output = output; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
