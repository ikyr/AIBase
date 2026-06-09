package com.datang.aibase.mcp.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.mcp.entity.McpServer;
import com.datang.aibase.mcp.entity.McpTool;
import com.datang.aibase.mcp.service.McpService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @GetMapping("/servers")
    public ApiResponse<List<McpServer>> listAll() {
        return ApiResponse.ok(mcpService.listAll());
    }

    @GetMapping("/servers/{id}")
    public ApiResponse<McpServer> getById(@PathVariable String id) {
        return ApiResponse.ok(mcpService.getById(id));
    }

    @PostMapping("/servers")
    public ApiResponse<McpServer> register(@RequestBody McpServer server) {
        return ApiResponse.ok(mcpService.register(server));
    }

    @DeleteMapping("/servers/{id}")
    public ApiResponse<Void> deleteServer(@PathVariable String id) {
        mcpService.deleteServer(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/servers/{id}/tools")
    public ApiResponse<List<McpTool>> listTools(@PathVariable String id) {
        return ApiResponse.ok(mcpService.listTools(id));
    }

    @GetMapping("/tools")
    public ApiResponse<List<McpTool>> listAllTools() {
        return ApiResponse.ok(mcpService.listAllTools());
    }

    @PostMapping("/servers/{id}/tools")
    public ApiResponse<McpTool> addTool(@PathVariable String id, @RequestBody McpTool tool) {
        return ApiResponse.ok(mcpService.addTool(id, tool));
    }

    @PostMapping("/tools/{toolId}/invoke")
    public ApiResponse<Map<String, Object>> invokeTool(@PathVariable String toolId, @RequestBody Map<String, Object> request) {
        return ApiResponse.ok(mcpService.invokeTool(toolId, request));
    }
}
