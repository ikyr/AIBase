package com.datang.aibase.api.dto;

import java.util.List;

public class AgentChatResponse {
    private String sessionId;
    private String messageId;
    private String content;
    private List<ToolCallRecord> toolCalls;
    private String finishReason;
    private int tokensUsed;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<ToolCallRecord> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCallRecord> toolCalls) { this.toolCalls = toolCalls; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }

    public static class ToolCallRecord {
        private String toolName;
        private String input;
        private String output;
        public String getToolName() { return toolName; }
        public void setToolName(String toolName) { this.toolName = toolName; }
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }
}
