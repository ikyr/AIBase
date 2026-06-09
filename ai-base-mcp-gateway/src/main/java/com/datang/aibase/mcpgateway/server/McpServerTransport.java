package com.datang.aibase.mcpgateway.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class McpServerTransport {

    private static final Logger log = LoggerFactory.getLogger(McpServerTransport.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String PROTOCOL_VERSION = "2024-11-05";

    private final LocalToolRegistry toolRegistry;
    private final Map<String, SseEmitter> sessions = new ConcurrentHashMap<>();

    public McpServerTransport(LocalToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public SseEmitter createSession(String sessionId) {
        SseEmitter emitter = new SseEmitter(0L);
        sessions.put(sessionId, emitter);

        emitter.onCompletion(() -> sessions.remove(sessionId));
        emitter.onTimeout(() -> sessions.remove(sessionId));
        emitter.onError(e -> sessions.remove(sessionId));

        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/mcp/message?sessionId=" + sessionId));
        } catch (IOException e) {
            log.warn("Failed to send endpoint event for session {}", sessionId);
        }

        log.info("MCP SSE session created: {}", sessionId);
        return emitter;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> processMessage(String sessionId, Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");

        try {
            Map<String, Object> result = switch (method) {
                case "initialize" -> handleInitialize(request);
                case "ping" -> Map.of();
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolsCall((Map<String, Object>) request.get("params"));
                default -> throw new IllegalArgumentException("Unknown method: " + method);
            };

            return buildResponse(id, result, null);
        } catch (Exception e) {
            log.error("Error processing MCP method {}: {}", method, e.getMessage());
            return buildResponse(id, null, Map.of(
                    "code", -32603,
                    "message", e.getMessage() != null ? e.getMessage() : "Internal error"
            ));
        }
    }

    public void sendToSession(String sessionId, Map<String, Object> message) {
        SseEmitter emitter = sessions.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(mapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.warn("Failed to send SSE to session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    public void closeSession(String sessionId) {
        SseEmitter emitter = sessions.remove(sessionId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Session {} already closed", sessionId);
            }
        }
    }

    private Map<String, Object> handleInitialize(Map<String, Object> request) {
        return Map.of(
                "protocolVersion", PROTOCOL_VERSION,
                "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of(
                        "name", "aibase-mcp-gateway",
                        "version", "1.0.0"
                )
        );
    }

    private Map<String, Object> handleToolsList() {
        List<ExportableTool> tools = toolRegistry.listAll();
        List<Map<String, Object>> toolList = tools.stream()
                .map(t -> Map.<String, Object>of(
                        "name", t.getName(),
                        "description", t.getDescription(),
                        "inputSchema", t.getInputSchema()
                ))
                .toList();

        return Map.of("tools", toolList);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolsCall(Map<String, Object> params) {
        String toolName = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());

        ExportableTool tool = toolRegistry.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }

        Map<String, Object> result = tool.execute(arguments);

        return Map.of("content", List.of(Map.of(
                "type", "text",
                "text", safeSerialize(result)
        )));
    }

    private Map<String, Object> buildResponse(Object id, Map<String, Object> result, Map<String, Object> error) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        if (error != null) {
            response.put("error", error);
        } else {
            response.put("result", result != null ? result : Map.of());
        }
        if (id != null) {
            response.put("id", id);
        }
        return response;
    }

    private String safeSerialize(Object obj) {
        try {
            return obj instanceof String ? (String) obj : mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
