package com.datang.aibase.mcpgateway.client;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.mcpgateway.entity.McpClientConn;
import com.datang.aibase.mcpgateway.entity.McpServerReg;
import com.datang.aibase.mcpgateway.entity.McpToolReg;
import com.datang.aibase.mcpgateway.mapper.McpClientConnMapper;
import com.datang.aibase.mcpgateway.mapper.McpToolRegMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class McpClientManager {

    private static final Logger log = LoggerFactory.getLogger(McpClientManager.class);

    private final McpClientConnMapper connMapper;
    private final McpToolRegMapper toolRegMapper;
    private final Map<String, RestClient> clients = new ConcurrentHashMap<>();

    public McpClientManager(McpClientConnMapper connMapper, McpToolRegMapper toolRegMapper) {
        this.connMapper = connMapper;
        this.toolRegMapper = toolRegMapper;
    }

    public McpClientConn connect(McpServerReg server) {
        McpClientConn existing = connMapper.selectActiveByServerId(server.getId());
        if (existing != null) {
            return existing;
        }

        McpClientConn conn = new McpClientConn();
        conn.setId(SnowflakeIdGenerator.nextId());
        conn.setServerId(server.getId());
        conn.setStatus("CONNECTING");

        try {
            RestClient client = RestClient.create(server.getEndpoint());
            String sessionToken = performHandshake(client, server);

            conn.setSessionToken(sessionToken);
            conn.setStatus("CONNECTED");
            conn.setConnectedAt(LocalDateTime.now());
            conn.setErrorCount(0);
            conn.setCreatedAt(LocalDateTime.now());
            conn.setUpdatedAt(LocalDateTime.now());
            connMapper.insert(conn);

            clients.put(server.getId(), client);
            log.info("Connected to MCP server: {} ({})", server.getName(), server.getEndpoint());
        } catch (Exception e) {
            conn.setStatus("DISCONNECTED");
            conn.setErrorCount(1);
            conn.setLastError(e.getMessage());
            conn.setCreatedAt(LocalDateTime.now());
            conn.setUpdatedAt(LocalDateTime.now());
            connMapper.insert(conn);
            log.error("Failed to connect to MCP server {}: {}", server.getName(), e.getMessage());
        }

        return conn;
    }

    public void disconnect(String serverId) {
        clients.remove(serverId);
        McpClientConn conn = connMapper.selectActiveByServerId(serverId);
        if (conn != null) {
            conn.setStatus("DISCONNECTED");
            conn.setDisconnectedAt(LocalDateTime.now());
            conn.setUpdatedAt(LocalDateTime.now());
            connMapper.update(conn);
        }
    }

    @SuppressWarnings("unchecked")
    public List<McpToolReg> discoverTools(McpServerReg server) {
        RestClient client = clients.get(server.getId());
        if (client == null) {
            client = RestClient.create(server.getEndpoint());
            clients.put(server.getId(), client);
        }

        try {
            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "method", "tools/list",
                    "id", "discover-" + System.currentTimeMillis()
            );

            Map<String, Object> response = client.post()
                    .uri(server.getEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("result")) {
                log.warn("No tools discovered from server {}", server.getName());
                return List.of();
            }

            Object result = response.get("result");
            if (!(result instanceof Map<?, ?> resultMap)) {
                return List.of();
            }

            Object tools = ((Map<String, Object>) resultMap).get("tools");
            if (!(tools instanceof List<?> toolList)) {
                return List.of();
            }

            List<McpToolReg> discovered = new ArrayList<>();
            for (Object t : toolList) {
                if (t instanceof Map<?, ?> tm) {
                    Map<String, Object> toolMap = (Map<String, Object>) tm;
                    McpToolReg reg = new McpToolReg();
                    reg.setId(SnowflakeIdGenerator.nextId());
                    reg.setServerId(server.getId());
                    reg.setToolName(String.valueOf(toolMap.getOrDefault("name", "")));
                    reg.setDescription(String.valueOf(toolMap.getOrDefault("description", "")));
                    reg.setInputSchema(String.valueOf(toolMap.getOrDefault("inputSchema", "{}")));
                    reg.setToolType("MCP_REMOTE");
                    reg.setSourceService(server.getName());
                    reg.setCreatedAt(LocalDateTime.now());
                    reg.setUpdatedAt(LocalDateTime.now());
                    discovered.add(reg);
                }
            }

            if (!discovered.isEmpty()) {
                toolRegMapper.deleteByServerId(server.getId());
                toolRegMapper.batchInsert(discovered);
            }
            log.info("Discovered {} tools from MCP server {}", discovered.size(), server.getName());
            return discovered;
        } catch (Exception e) {
            log.error("Failed to discover tools from {}: {}", server.getName(), e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> invokeTool(McpServerReg server, String toolName, Map<String, Object> params) {
        RestClient client = clients.get(server.getId());
        if (client == null) {
            client = RestClient.create(server.getEndpoint());
            clients.put(server.getId(), client);
        }

        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "method", "tools/call",
                "params", Map.of("name", toolName, "arguments", params),
                "id", "call-" + System.currentTimeMillis()
        );

        Map<String, Object> response = client.post()
                .uri(server.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return Map.of("error", "Empty response from MCP server");
        }

        if (response.containsKey("error")) {
            return Map.of("error", response.get("error").toString());
        }

        Object result = response.get("result");
        if (result instanceof Map<?, ?> rm) {
            return (Map<String, Object>) rm;
        }

        return Map.of("result", response.toString());
    }

    @SuppressWarnings("unchecked")
    public boolean healthCheck(McpServerReg server) {
        try {
            RestClient client = clients.get(server.getId());
            if (client == null) {
                client = RestClient.create(server.getEndpoint());
                clients.put(server.getId(), client);
            }

            Map<String, Object> ping = Map.of(
                    "jsonrpc", "2.0",
                    "method", "ping",
                    "id", "health-" + System.currentTimeMillis()
            );

            Map<String, Object> response = client.post()
                    .uri(server.getEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ping)
                    .retrieve()
                    .body(Map.class);

            return response != null && !response.containsKey("error");
        } catch (Exception e) {
            log.debug("Health check failed for {}: {}", server.getName(), e.getMessage());
            return false;
        }
    }

    String performHandshake(RestClient client, McpServerReg server) {
        Map<String, Object> initRequest = Map.of(
                "jsonrpc", "2.0",
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", "2024-11-05",
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "aibase-mcp-gateway", "version", "1.0.0")
                ),
                "id", "init-" + System.currentTimeMillis()
        );

        Map<String, Object> response = client.post()
                .uri(server.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .body(initRequest)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("result")) {
            return "session-" + System.currentTimeMillis();
        }
        return null;
    }
}
