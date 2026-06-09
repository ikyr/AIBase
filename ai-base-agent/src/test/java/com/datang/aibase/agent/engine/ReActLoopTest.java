package com.datang.aibase.agent.engine;

import com.datang.aibase.agent.entity.AgentDef;
import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.entity.AgentSession;
import com.datang.aibase.agent.tool.Tool;
import com.datang.aibase.agent.tool.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReActLoopTest {

    private ToolRegistry toolRegistry;
    private ReActLoop reActLoop;

    @BeforeEach
    void setUp() {
        toolRegistry = mock(ToolRegistry.class);
        reActLoop = new ReActLoop(toolRegistry);
    }

    @Test
    @DisplayName("run returns final answer message when LLM gives final answer directly")
    void run_directFinalAnswer_returnsAssistantMessage() {
        var agent = createAgent();
        var session = createSession();
        Function<List<Map<String, String>>, String> llmCaller = messages -> "Final Answer: Hello, how can I help?";
        when(toolRegistry.getFiltered(null)).thenReturn(Map.of());

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "Hi", llmCaller, null);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo("user");
        assertThat(result.getLast().getRole()).isEqualTo("assistant");
        assertThat(result.getLast().getContent()).isEqualTo("Hello, how can I help?");
    }

    @Test
    @DisplayName("run executes tool when LLM requests action")
    void run_toolAction_executesTool() {
        var agent = createAgent();
        var session = createSession();
        var tool = mock(Tool.class);
        when(tool.getName()).thenReturn("calculator");
        when(tool.getDescription()).thenReturn("evaluates math");
        when(tool.execute(Map.of("expression", "2+2"))).thenReturn(Map.of("result", 4));

        when(toolRegistry.getFiltered(Set.of("calculator"))).thenReturn(Map.of("calculator", tool));

        Function<List<Map<String, String>>, String> llmCaller = messages -> {
            String lastContent = messages.get(messages.size() - 1).get("content");
            if (lastContent.contains("Observation:")) {
                return "Final Answer: The result is 4";
            }
            return "Thought: I need to calculate\nAction: calculator|{\"expression\":\"2+2\"}";
        };

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "2+2", llmCaller, Set.of("calculator"));

        assertThat(result).anyMatch(m -> "tool_call".equals(m.getContentType()));
        assertThat(result.getLast().getContent()).isEqualTo("The result is 4");
    }

    @Test
    @DisplayName("run stops at max iterations and returns limit message")
    void run_maxIterations_returnsLimit() {
        var agent = createAgent();
        var session = createSession();
        when(toolRegistry.getFiltered(null)).thenReturn(Map.of());

        Function<List<Map<String, String>>, String> llmCaller = messages ->
                "Thought: still thinking...\nAction: unknown|{}";

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "complex question", llmCaller, null);

        assertThat(result.getLast().getContent()).contains("maximum thinking iterations");
    }

    @Test
    @DisplayName("run handles LLM exception gracefully")
    void run_llmException_returnsError() {
        var agent = createAgent();
        var session = createSession();
        when(toolRegistry.getFiltered(null)).thenReturn(Map.of());

        Function<List<Map<String, String>>, String> llmCaller = messages -> {
            throw new RuntimeException("LLM unavailable");
        };

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "hello", llmCaller, null);

        assertThat(result).anyMatch(m -> m.getContent().contains("Error calling model"));
    }

    @Test
    @DisplayName("run handles tool execution failure gracefully")
    void run_toolFailure_continues() {
        var agent = createAgent();
        var session = createSession();
        var tool = mock(Tool.class);
        when(tool.getName()).thenReturn("calculator");
        when(tool.getDescription()).thenReturn("evaluates math");
        when(tool.execute(Map.of())).thenThrow(new RuntimeException("tool crashed"));

        when(toolRegistry.getFiltered(Set.of("calculator"))).thenReturn(Map.of("calculator", tool));

        Function<List<Map<String, String>>, String> llmCaller = messages -> {
            String lastContent = messages.get(messages.size() - 1).get("content");
            if (lastContent.contains("Observation:")) {
                return "Final Answer: Tool failed but I'll try my best";
            }
            return "Thought: use calculator\nAction: calculator|{}";
        };

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "calculate", llmCaller, Set.of("calculator"));

        assertThat(result).anyMatch(m -> m.getContent() != null && m.getContent().contains("Tool execution error"));
    }

    @Test
    @DisplayName("run handles tool not found in registry")
    void run_toolNotFound_returnsToolNotFound() {
        var agent = createAgent();
        var session = createSession();
        when(toolRegistry.getFiltered(Set.of())).thenReturn(Map.of());

        Function<List<Map<String, String>>, String> llmCaller = messages -> {
            String lastContent = messages.get(messages.size() - 1).get("content");
            if (lastContent.contains("Observation:")) {
                return "Final Answer: Tool was not available";
            }
            return "Thought: I need a tool\nAction: missing_tool|{}";
        };

        List<AgentMessage> result = reActLoop.run(agent, session, List.of(),
                "do something", llmCaller, Set.of());

        assertThat(result).anyMatch(m -> m.getContent() != null && m.getContent().contains("Tool not found"));
    }

    private AgentDef createAgent() {
        AgentDef agent = new AgentDef();
        agent.setId("agent-1");
        agent.setName("Test Agent");
        agent.setSystemPrompt("You are a helpful assistant.");
        return agent;
    }

    private AgentSession createSession() {
        AgentSession session = new AgentSession();
        session.setId("session-1");
        session.setAgentId("agent-1");
        return session;
    }
}
