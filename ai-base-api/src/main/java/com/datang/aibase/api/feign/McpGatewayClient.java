package com.datang.aibase.api.feign;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.dto.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "mcp-gateway", path = "/api/v1/mcp")
public interface McpGatewayClient {

    @PostMapping("/servers")
    ApiResponse<Map<String, Object>> registerServer(@RequestBody Map<String, Object> request);

    @GetMapping("/servers")
    ApiResponse<PageResult<Map<String, Object>>> listServers(@RequestParam int page, @RequestParam int size);

    @GetMapping("/servers/{serverId}")
    ApiResponse<Map<String, Object>> getServer(@PathVariable String serverId);

    @DeleteMapping("/servers/{serverId}")
    ApiResponse<Void> unregisterServer(@PathVariable String serverId);

    @GetMapping("/tools")
    ApiResponse<PageResult<Map<String, Object>>> listTools(@RequestParam int page, @RequestParam int size);

    @PostMapping("/tools/{toolId}/invoke")
    ApiResponse<Map<String, Object>> invokeTool(@PathVariable String toolId, @RequestBody Map<String, Object> request);
}
