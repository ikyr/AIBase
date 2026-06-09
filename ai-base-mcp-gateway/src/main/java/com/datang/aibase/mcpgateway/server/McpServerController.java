package com.datang.aibase.mcpgateway.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mcp")
public class McpServerController {

    private static final Logger log = LoggerFactory.getLogger(McpServerController.class);

    private final McpServerTransport transport;

    public McpServerController(McpServerTransport transport) {
        this.transport = transport;
    }

    @GetMapping("/sse")
    public SseEmitter connect() {
        String sessionId = UUID.randomUUID().toString();
        return transport.createSession(sessionId);
    }

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> message(@RequestParam String sessionId, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = transport.processMessage(sessionId, request);
        transport.sendToSession(sessionId, response);
        return Map.of("jsonrpc", "2.0", "id", request.getOrDefault("id", null));
    }
}
