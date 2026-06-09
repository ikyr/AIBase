package com.datang.aibase.api.controller;

import com.datang.aibase.api.dto.AgentChatRequest;
import com.datang.aibase.api.dto.AgentChatResponse;
import com.datang.aibase.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final RestClient agentClient;

    public ChatController(@Value("${agent.url:http://localhost:8105}") String agentUrl) {
        this.agentClient = RestClient.create(agentUrl);
    }

    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> send(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String agentId = request.getOrDefault("agentId", "default");
        String sessionId = request.getOrDefault("sessionId", null);

        AgentChatRequest chatRequest = new AgentChatRequest();
        chatRequest.setAgentId(agentId);
        chatRequest.setMessage(message);
        if (sessionId != null && !sessionId.isBlank()) {
            chatRequest.setSessionId(sessionId);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = agentClient.post()
                    .uri("/api/v1/agent/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(chatRequest)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return ApiResponse.error("EMPTY_RESPONSE", "Empty response from agent service");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            Object data = response.get("data");
            if (data instanceof Map<?, ?> dm) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dm;
                result.put("reply", dataMap.getOrDefault("content", ""));
                result.put("sessionId", dataMap.getOrDefault("sessionId", ""));
                result.put("messageId", dataMap.getOrDefault("messageId", ""));
                result.put("finishReason", dataMap.getOrDefault("finishReason", ""));
                if (dataMap.containsKey("toolCalls")) {
                    result.put("toolCalls", dataMap.get("toolCalls"));
                }
            } else {
                result.put("reply", response.toString());
            }
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("Failed to call agent service: {}", e.getMessage());
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("reply", "抱歉，AI 服务暂时不可用，请稍后重试。");
            fallback.put("error", e.getMessage());
            return ApiResponse.ok(fallback);
        }
    }

    @GetMapping("/sessions")
    public ApiResponse<List<Map<String, Object>>> sessions() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = agentClient.get()
                    .uri("/api/v1/agent/sessions?limit=20")
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sessions = (List<Map<String, Object>>) list;
                return ApiResponse.ok(sessions);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch sessions from agent service: {}", e.getMessage());
        }
        return ApiResponse.ok(List.of());
    }
}
