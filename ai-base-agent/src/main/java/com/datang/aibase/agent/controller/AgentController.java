package com.datang.aibase.agent.controller;

import com.datang.aibase.agent.entity.AgentDef;
import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.entity.AgentSession;
import com.datang.aibase.agent.service.AgentService;
import com.datang.aibase.api.dto.AgentChatRequest;
import com.datang.aibase.api.dto.AgentChatResponse;
import com.datang.aibase.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public ApiResponse<List<AgentDef>> listAll() {
        return ApiResponse.ok(agentService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentDef> getById(@PathVariable String id) {
        return ApiResponse.ok(agentService.getById(id));
    }

    @PostMapping
    public ApiResponse<AgentDef> create(@RequestBody AgentDef agent) {
        return ApiResponse.ok(agentService.create(agent));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AgentSession>> listSessions(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(agentService.listSessions(limit));
    }

    @PostMapping("/{id}/sessions")
    public ApiResponse<AgentSession> createSession(@PathVariable String id, @RequestBody AgentSession session) {
        return ApiResponse.ok(agentService.createSession(id, session.getTitle()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<AgentMessage>> getMessages(@PathVariable String sessionId) {
        return ApiResponse.ok(agentService.getMessages(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<AgentMessage> addMessage(@PathVariable String sessionId, @RequestBody AgentMessage message) {
        return ApiResponse.ok(agentService.addMessage(sessionId, message));
    }

    @PostMapping("/{id}/chat")
    public ApiResponse<AgentChatResponse> chat(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String sessionId = body.get("sessionId") != null ? body.get("sessionId").toString() : null;
        String message = body.get("message") != null ? body.get("message").toString() : "";
        return ApiResponse.ok(agentService.chat(id, sessionId, message));
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chatByRequest(@RequestBody AgentChatRequest request) {
        return ApiResponse.ok(agentService.chat(request.getAgentId(), request.getSessionId(), request.getMessage()));
    }

    @PostMapping("/sessions/{sessionId}/branch")
    public ApiResponse<AgentMessage> branch(@PathVariable String sessionId, @RequestBody Map<String, Object> body) {
        String parentMessageId = body.get("parentMessageId") != null ? body.get("parentMessageId").toString() : null;
        if (parentMessageId == null) throw new IllegalArgumentException("parentMessageId is required");
        return ApiResponse.ok(agentService.branch(sessionId, parentMessageId));
    }

    @GetMapping("/sessions/{sessionId}/tree")
    public ApiResponse<Map<String, Object>> getTree(@PathVariable String sessionId) {
        Map<String, List<AgentMessage>> branches = agentService.getBranchPoints(sessionId);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("branches", branches);
        result.put("branchCount", branches.size());
        return ApiResponse.ok(result);
    }
}
