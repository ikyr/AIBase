package com.datang.aibase.agent.memory;

import com.datang.aibase.agent.entity.AgentMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContextWindowManagerTest {

    private ContextWindowManager manager;

    @BeforeEach
    void setUp() {
        manager = new ContextWindowManager(4000);
    }

    @Test
    @DisplayName("trim returns all messages when under budget")
    void trim_underBudget_returnsAll() {
        List<AgentMessage> history = createMessages(5, 100);

        List<AgentMessage> result = manager.trim(history, 1000);

        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("trim keeps most recent messages when over budget")
    void trim_overBudget_keepsRecent() {
        List<AgentMessage> history = createMessages(50, 200);

        List<AgentMessage> result = manager.trim(history, 1000);

        assertThat(result.size()).isLessThan(50);
        assertThat(result.get(result.size() - 1).getContent())
            .isEqualTo(history.get(history.size() - 1).getContent());
    }

    @Test
    @DisplayName("trim respects tokenCount field when set")
    void trim_respectsTokenCount() {
        List<AgentMessage> history = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            AgentMessage msg = new AgentMessage();
            msg.setRole("user");
            msg.setContent("short");
            msg.setTokenCount(2000);
            history.add(msg);
        }

        List<AgentMessage> result = manager.trim(history, 1000);

        assertThat(result.size()).isLessThan(3);
    }

    @Test
    @DisplayName("summarizeOlder builds summary from old messages")
    void summarizeOlder_buildsSummary() {
        List<AgentMessage> oldMessages = List.of(
            createMsg("user", "What is the weather?"),
            createMsg("assistant", "The weather is sunny today.")
        );

        String summary = manager.summarizeOlder(oldMessages, "Current context");

        assertThat(summary).contains("[Previous conversation summary]");
        assertThat(summary).contains("Current context");
    }

    @Test
    @DisplayName("summarizeOlder returns latestContext when oldMessages empty")
    void summarizeOlder_empty_returnsLatest() {
        String result = manager.summarizeOlder(List.of(), "standalone");

        assertThat(result).isEqualTo("standalone");
    }

    @Test
    @DisplayName("estimateTotalTokens sums all message tokens")
    void estimateTotalTokens_sumsAll() {
        List<AgentMessage> messages = createMessages(10, 40);

        int total = manager.estimateTotalTokens(messages);

        assertThat(total).isGreaterThan(0);
    }

    private List<AgentMessage> createMessages(int count, int contentLength) {
        List<AgentMessage> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(createMsg(i % 2 == 0 ? "user" : "assistant",
                "message ".repeat(contentLength / 8) + i));
        }
        return messages;
    }

    private AgentMessage createMsg(String role, String content) {
        AgentMessage msg = new AgentMessage();
        msg.setRole(role);
        msg.setContent(content);
        return msg;
    }
}
