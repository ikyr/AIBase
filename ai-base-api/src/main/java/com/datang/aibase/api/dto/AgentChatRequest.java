package com.datang.aibase.api.dto;

public class AgentChatRequest {
    private String agentId;
    private String sessionId;
    private String message;
    private boolean stream;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
}
