package com.datang.aibase.agent.memory;

import com.datang.aibase.agent.entity.AgentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContextWindowManager {

    private static final Logger log = LoggerFactory.getLogger(ContextWindowManager.class);
    private static final int DEFAULT_MAX_TOKENS = 8000;
    private static final int CHARS_PER_TOKEN = 4;

    private final int maxTokens;

    public ContextWindowManager() {
        this(DEFAULT_MAX_TOKENS);
    }

    public ContextWindowManager(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<AgentMessage> trim(List<AgentMessage> history, int estimatedPromptTokens) {
        int budget = maxTokens - estimatedPromptTokens - 1000;
        if (budget <= 0) budget = 2000;

        int totalTokens = 0;
        List<AgentMessage> result = new ArrayList<>();

        for (int i = history.size() - 1; i >= 0; i--) {
            AgentMessage msg = history.get(i);
            int msgTokens = estimateTokens(msg);
            if (totalTokens + msgTokens <= budget) {
                result.addFirst(msg);
                totalTokens += msgTokens;
            } else {
                break;
            }
        }

        if (result.size() < history.size()) {
            log.debug("Trimmed {} messages to {} ({} tokens / {} budget)",
                    history.size(), result.size(), totalTokens, budget);
        }
        return result;
    }

    public String summarizeOlder(List<AgentMessage> oldMessages, String latestContext) {
        if (oldMessages.isEmpty()) return latestContext;
        StringBuilder sb = new StringBuilder("[Previous conversation summary]\n");
        for (AgentMessage msg : oldMessages) {
            if (msg.getContent() != null && msg.getContent().length() > 200) {
                sb.append(msg.getRole()).append(": ")
                        .append(msg.getContent(), 0, 200).append("...\n");
            } else if (msg.getContent() != null) {
                sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }
        sb.append("\n---\n").append(latestContext);
        return sb.toString();
    }

    public int estimateTotalTokens(List<AgentMessage> messages) {
        return messages.stream().mapToInt(this::estimateTokens).sum();
    }

    private int estimateTokens(AgentMessage msg) {
        if (msg.getTokenCount() != null && msg.getTokenCount() > 0) {
            return msg.getTokenCount();
        }
        int chars = 0;
        if (msg.getContent() != null) chars += msg.getContent().length();
        if (msg.getToolCalls() != null) chars += msg.getToolCalls().length();
        return Math.max(1, chars / CHARS_PER_TOKEN);
    }
}
