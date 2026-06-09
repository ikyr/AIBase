package com.datang.aibase.mcp.service;

import com.datang.aibase.mcp.entity.McpServer;
import com.datang.aibase.mcp.entity.McpTool;

import java.util.List;
import java.util.Map;

public interface McpService {

    List<McpServer> listAll();

    McpServer getById(String id);

    McpServer register(McpServer server);

    void deleteServer(String id);

    List<McpTool> listTools(String serverId);

    List<McpTool> listAllTools();

    McpTool addTool(String serverId, McpTool tool);

    Map<String, Object> invokeTool(String toolId, Map<String, Object> params);
}
