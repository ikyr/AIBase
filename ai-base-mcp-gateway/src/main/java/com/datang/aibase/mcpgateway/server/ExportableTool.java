package com.datang.aibase.mcpgateway.server;

import java.util.Map;

/**
 * Interface for tools that can be exported via the MCP Server.
 * Implement this interface and register as a Spring Bean to expose a tool.
 */
public interface ExportableTool {

    String getName();

    String getDescription();

    Map<String, Object> getInputSchema();

    Map<String, Object> execute(Map<String, Object> arguments);
}
