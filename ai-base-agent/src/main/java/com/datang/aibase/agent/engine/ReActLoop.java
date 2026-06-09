package com.datang.aibase.agent.engine;

import com.datang.aibase.agent.entity.AgentDef;
import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.entity.AgentSession;
import com.datang.aibase.agent.tool.Tool;
import com.datang.aibase.agent.tool.ToolRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Component
public class ReActLoop {

    private static final Logger log = LoggerFactory.getLogger(ReActLoop.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_ITERATIONS = 10;

    private static final String REACT_PROMPT = """
            You are an AI assistant. Follow this format:

            Thought: <reason about what to do next>
            Action: <tool name>|<JSON parameters>
            Observation: <result of the action>
            ... (repeat Thought/Action/Observation as needed)
            Thought: I now have the answer
            Final Answer: <response to the user>

            Available tools:
            %s

            Begin!
            """;

    private final ToolRegistry toolRegistry;

    public ReActLoop(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public List<AgentMessage> run(AgentDef agent, AgentSession session,
                                   List<AgentMessage> history, String userMessage,
                                   Function<List<Map<String, String>>, String> llmCaller,
                                   Set<String> allowedToolNames) {
        List<AgentMessage> newMessages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        AgentMessage userMsg = createMessage(session.getId(), null, "user", userMessage, "text");
        newMessages.add(userMsg);

        Map<String, Tool> activeTools = toolRegistry.getFiltered(allowedToolNames);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(agent, activeTools)));

        for (AgentMessage m : history) {
            messages.add(Map.of("role", m.getRole(), "content", m.getContent() != null ? m.getContent() : ""));
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        String currentResponse;
        int iteration = 0;
        String lastThought = "";

        while (iteration < MAX_ITERATIONS) {
            iteration++;
            log.debug("ReAct iteration {} for session {}", iteration, session.getId());

            try {
                currentResponse = llmCaller.apply(messages);
            } catch (Exception e) {
                log.error("LLM call failed at iteration {}: {}", iteration, e.getMessage());
                AgentMessage errorMsg = createMessage(session.getId(), userMsg.getId(), "assistant",
                        "Error calling model: " + e.getMessage(), "text");
                newMessages.add(errorMsg);
                break;
            }

            ParsedResponse parsed = parseResponse(currentResponse);

            if (parsed.finalAnswer() != null) {
                AgentMessage assistantMsg = createMessage(session.getId(), userMsg.getId(), "assistant",
                        parsed.finalAnswer(), "text");
                assistantMsg.setTokenCount(estimateTokens(currentResponse));
                newMessages.add(assistantMsg);
                break;
            }

            if (parsed.thought() != null) {
                lastThought = parsed.thought();
            }

            if (parsed.action() != null) {
                String toolName = parsed.action();
                Map<String, Object> params = parsed.parameters() != null ? parsed.parameters() : Map.of();
                String observation = executeTool(toolName, params, activeTools);

                AgentMessage toolMsg = createMessage(session.getId(), userMsg.getId(), "tool",
                        observation, "tool_call");
                try {
                    toolMsg.setToolCalls(mapper.writeValueAsString(Map.of(
                            "tool", toolName,
                            "parameters", params,
                            "result", observation
                    )));
                } catch (Exception ignored) {}
                newMessages.add(toolMsg);

                messages.add(Map.of("role", "assistant", "content",
                        "Thought: " + (lastThought.isBlank() ? "I need to use " + toolName : lastThought) +
                                "\nAction: " + toolName + "|" + toParamString(params)));
                messages.add(Map.of("role", "user", "content", "Observation: " + observation));
            } else if (parsed.finalAnswer() == null) {
                AgentMessage assistantMsg = createMessage(session.getId(), userMsg.getId(), "assistant",
                        currentResponse, "text");
                assistantMsg.setTokenCount(estimateTokens(currentResponse));
                newMessages.add(assistantMsg);
                break;
            }
        }

        if (iteration >= MAX_ITERATIONS) {
            AgentMessage limitMsg = createMessage(session.getId(), userMsg.getId(), "assistant",
                    "Reached maximum thinking iterations. Last thought: " + lastThought, "text");
            newMessages.add(limitMsg);
        }

        return newMessages;
    }

    private String buildSystemPrompt(AgentDef agent, Map<String, Tool> activeTools) {
        String prompt = agent.getSystemPrompt() != null ? agent.getSystemPrompt() : "";

        StringBuilder toolSection = new StringBuilder();
        if (activeTools.isEmpty()) {
            toolSection.append("None");
        } else {
            for (Tool tool : activeTools.values()) {
                toolSection.append("- ").append(tool.getName()).append(": ")
                        .append(tool.getDescription()).append("\n");
            }
        }

        return prompt + "\n\n" + String.format(REACT_PROMPT, toolSection.toString().stripTrailing());
    }

    private ParsedResponse parseResponse(String text) {
        if (text == null || text.isBlank()) return ParsedResponse.EMPTY;

        String thought = null;
        String action = null;
        String finalAnswer = null;
        Map<String, Object> params = null;

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase().startsWith("thought:")) {
                thought = trimmed.substring(8).trim();
            } else if (trimmed.toLowerCase().startsWith("action:")) {
                String actionStr = trimmed.substring(7).trim();
                int pipeIdx = actionStr.indexOf('|');
                if (pipeIdx > 0) {
                    action = actionStr.substring(0, pipeIdx).trim();
                    String paramsStr = actionStr.substring(pipeIdx + 1).trim();
                    params = parseParams(paramsStr);
                } else {
                    action = actionStr;
                }
            } else if (trimmed.toLowerCase().startsWith("final answer:")) {
                finalAnswer = trimmed.substring(13).trim();
            }
        }

        return new ParsedResponse(thought, action, params, finalAnswer);
    }

    private Map<String, Object> parseParams(String paramsStr) {
        if (paramsStr == null || paramsStr.isBlank()) return Map.of();
        try {
            return mapper.readValue(paramsStr, new TypeReference<>() {});
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (String part : paramsStr.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    result.put(kv[0].trim(), kv[1].trim().replace("\"", ""));
                }
            }
            return result;
        }
    }

    private String toParamString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "{}";
        try {
            return mapper.writeValueAsString(params);
        } catch (Exception e) {
            return params.toString();
        }
    }

    private String executeTool(String toolName, Map<String, Object> params, Map<String, Tool> activeTools) {
        Tool tool = activeTools.get(toolName);
        if (tool == null) {
            return "Tool not found: " + toolName + ". Available: " + activeTools.keySet();
        }
        try {
            Map<String, Object> result = tool.execute(params);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", toolName, e.getMessage());
            return "Tool execution error: " + e.getMessage();
        }
    }

    private AgentMessage createMessage(String sessionId, String parentId, String role, String content, String contentType) {
        AgentMessage msg = new AgentMessage();
        msg.setSessionId(sessionId);
        msg.setParentId(parentId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setContentType(contentType);
        return msg;
    }

    private int estimateTokens(String text) {
        return text == null ? 0 : text.length() / 4;
    }

    record ParsedResponse(String thought, String action, Map<String, Object> parameters, String finalAnswer) {
        static final ParsedResponse EMPTY = new ParsedResponse(null, null, null, null);
    }
}
