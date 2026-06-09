package com.datang.aibase.api.dto;
import java.util.Map;
public class WorkflowExecuteRequest {
    private String definitionId;
    private Map<String,Object> input;
    private boolean async;
    public String getDefinitionId() { return definitionId; }
    public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }
    public Map<String,Object> getInput() { return input; }
    public void setInput(Map<String,Object> input) { this.input = input; }
    public boolean isAsync() { return async; }
    public void setAsync(boolean async) { this.async = async; }
}
