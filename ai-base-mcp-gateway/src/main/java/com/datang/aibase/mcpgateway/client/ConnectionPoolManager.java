package com.datang.aibase.mcpgateway.client;

import com.datang.aibase.mcpgateway.entity.McpServerReg;
import com.datang.aibase.mcpgateway.mapper.McpServerRegMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionPoolManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolManager.class);
    private static final int MAX_RETRIES = 3;
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;

    private final McpServerRegMapper serverRegMapper;
    private final McpClientManager clientManager;
    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();

    public ConnectionPoolManager(McpServerRegMapper serverRegMapper, McpClientManager clientManager) {
        this.serverRegMapper = serverRegMapper;
        this.clientManager = clientManager;
    }

    @Scheduled(fixedDelay = 30_000)
    public void healthCheck() {
        List<McpServerReg> servers = serverRegMapper.selectAll();
        for (McpServerReg server : servers) {
            if (!"ACTIVE".equals(server.getStatus())) {
                continue;
            }

            boolean healthy = clientManager.healthCheck(server);

            if (healthy) {
                failureCounts.remove(server.getId());
                server.setHealthStatus("HEALTHY");
            } else {
                int failures = failureCounts.merge(server.getId(), 1, Integer::sum);
                if (failures >= 5) {
                    server.setHealthStatus("UNHEALTHY");
                    log.warn("MCP server {} marked UNHEALTHY after {} consecutive failures",
                            server.getName(), failures);
                } else {
                    server.setHealthStatus("DEGRADED");
                }
            }

            server.setLastHealthCheck(LocalDateTime.now());
            server.setUpdatedAt(LocalDateTime.now());
            serverRegMapper.updateHealth(server);
        }
    }

    public boolean isCircuitOpen(String serverId) {
        McpServerReg server = serverRegMapper.selectById(serverId);
        if (server == null) return true;
        return "UNHEALTHY".equals(server.getHealthStatus());
    }

    public int getRetryDelay(String serverId) {
        int failures = failureCounts.getOrDefault(serverId, 0);
        int delay = (int) Math.min(60_000, Math.pow(2, failures) * 1000);
        return delay;
    }
}
