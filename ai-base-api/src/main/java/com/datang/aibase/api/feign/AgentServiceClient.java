package com.datang.aibase.api.feign;
import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "agent-service", path = "/api/v1/agent")
public interface AgentServiceClient {
    @PostMapping("/chat")
    ApiResponse<AgentChatResponse> chat(@RequestBody AgentChatRequest request);
    @GetMapping("/session/{sessionId}")
    ApiResponse<SessionDetail> getSession(@PathVariable String sessionId);
}
