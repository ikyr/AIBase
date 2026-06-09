package com.datang.aibase.mcpgateway.server;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalToolRegistry {

    private final Map<String, ExportableTool> tools = new ConcurrentHashMap<>();

    public LocalToolRegistry(ObjectProvider<ExportableTool> exportableTools) {
        exportableTools.forEach(tool -> tools.put(tool.getName(), tool));
    }

    public ExportableTool get(String name) {
        return tools.get(name);
    }

    public List<ExportableTool> listAll() {
        return List.copyOf(tools.values());
    }

    public int count() {
        return tools.size();
    }
}
