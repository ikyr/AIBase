package com.datang.aibase.mcp.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.mcp.entity.McpServer;
import com.datang.aibase.mcp.entity.McpTool;
import com.datang.aibase.mcp.mapper.McpServerMapper;
import com.datang.aibase.mcp.mapper.McpToolMapper;
import com.datang.aibase.mcp.service.McpService;
import com.datang.aibase.mcpgateway.client.McpClientManager;
import com.datang.aibase.mcpgateway.entity.McpAudit;
import com.datang.aibase.mcpgateway.entity.McpServerReg;
import com.datang.aibase.mcpgateway.entity.McpToolReg;
import com.datang.aibase.mcpgateway.mapper.McpAuditMapper;
import com.datang.aibase.mcpgateway.mapper.McpServerRegMapper;
import com.datang.aibase.mcpgateway.mapper.McpToolRegMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class McpServiceImpl implements McpService {

    private static final Logger log = LoggerFactory.getLogger(McpServiceImpl.class);

    private final McpServerMapper serverMapper;
    private final McpToolMapper toolMapper;
    private final McpServerRegMapper serverRegMapper;
    private final McpToolRegMapper toolRegMapper;
    private final McpAuditMapper auditMapper;
    private final McpClientManager clientManager;

    public McpServiceImpl(McpServerMapper serverMapper, McpToolMapper toolMapper,
                          McpServerRegMapper serverRegMapper, McpToolRegMapper toolRegMapper,
                          McpAuditMapper auditMapper, McpClientManager clientManager) {
        this.serverMapper = serverMapper;
        this.toolMapper = toolMapper;
        this.serverRegMapper = serverRegMapper;
        this.toolRegMapper = toolRegMapper;
        this.auditMapper = auditMapper;
        this.clientManager = clientManager;
    }

    @Override
    public List<McpServer> listAll() {
        return serverMapper.selectAll();
    }

    @Override
    public McpServer getById(String id) {
        return serverMapper.selectById(id);
    }

    @Override
    public McpServer updateServer(String id, McpServer server) {
        server.setId(id);
        server.setUpdatedAt(java.time.LocalDateTime.now());
        serverMapper.update(server);
        return serverMapper.selectById(id);
    }

    @Override
    public McpServer register(McpServer server) {
        server.setId(SnowflakeIdGenerator.nextId());
        serverMapper.insert(server);

        McpServerReg reg = toServerReg(server);
        serverRegMapper.insert(reg);

        clientManager.connect(reg);
        clientManager.discoverTools(reg);

        return server;
    }

    @Override
    public void deleteServer(String id) {
        clientManager.disconnect(id);
        toolRegMapper.deactivateByServerId(id);
        serverRegMapper.softDelete(id);
        serverMapper.delete(id);
    }

    @Override
    public List<McpTool> listTools(String serverId) {
        return toolMapper.selectByServerId(serverId);
    }

    @Override
    public List<McpTool> listAllTools() {
        return toolMapper.selectAll();
    }

    @Override
    public McpTool addTool(String serverId, McpTool tool) {
        tool.setId(SnowflakeIdGenerator.nextId());
        tool.setServerId(serverId);
        toolMapper.insert(tool);
        return tool;
    }

    @Override
    public Map<String, Object> invokeTool(String toolId, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        String errorMsg = null;
        Map<String, Object> result;

        McpToolReg toolReg = toolRegMapper.selectById(toolId);
        if (toolReg == null) {
            return Map.of("error", "Tool not found: " + toolId);
        }

        McpServerReg serverReg = serverRegMapper.selectById(toolReg.getServerId());
        if (serverReg == null) {
            return Map.of("error", "Server not found for tool: " + toolId);
        }

        try {
            result = new LinkedHashMap<>(clientManager.invokeTool(serverReg, toolReg.getToolName(), params));
            result.put("toolName", toolReg.getToolName());
            result.put("serverName", serverReg.getName());
        } catch (Exception e) {
            status = "FAILED";
            errorMsg = e.getMessage();
            result = Map.of("error", e.getMessage());
        }

        recordAudit(serverReg.getId(), toolReg.getToolName(), params, result, status, errorMsg,
                (int) (System.currentTimeMillis() - start));
        return result;
    }

    private void recordAudit(String serverId, String toolName, Map<String, Object> input,
                              Map<String, Object> output, String status, String errorMsg, int durationMs) {
        try {
            McpAudit audit = new McpAudit();
            audit.setId(SnowflakeIdGenerator.nextId());
            audit.setServerId(serverId);
            audit.setToolName(toolName);
            audit.setInput(input.toString());
            audit.setOutput(output.toString());
            audit.setStatus(status);
            audit.setDurationMs(durationMs);
            audit.setErrorMsg(errorMsg);
            audit.setCreatedAt(LocalDateTime.now());
            auditMapper.insert(audit);
        } catch (Exception e) {
            log.warn("Failed to record MCP audit: {}", e.getMessage());
        }
    }

    private McpServerReg toServerReg(McpServer server) {
        McpServerReg reg = new McpServerReg();
        reg.setId(server.getId());
        reg.setName(server.getName());
        reg.setServerType(server.getServerType());
        reg.setTransport(server.getTransport());
        reg.setEndpoint(server.getEndpoint());
        reg.setCreatedAt(LocalDateTime.now());
        reg.setUpdatedAt(LocalDateTime.now());
        return reg;
    }
}
