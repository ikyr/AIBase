package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry(List<Tool> toolBeans) {
        for (Tool tool : toolBeans) {
            tools.put(tool.getName(), tool);
            log.info("Registered tool: {}", tool.getName());
        }
    }

    public Tool get(String name) {
        return tools.get(name);
    }

    public Set<String> getToolNames() {
        return tools.keySet();
    }

    public Map<String, Tool> getAll() {
        return Map.copyOf(tools);
    }

    public Map<String, Tool> getFiltered(Set<String> allowedNames) {
        if (allowedNames == null || allowedNames.isEmpty()) {
            return getAll();
        }
        Map<String, Tool> filtered = new LinkedHashMap<>();
        for (String name : allowedNames) {
            Tool tool = tools.get(name.trim());
            if (tool != null) {
                filtered.put(name.trim(), tool);
            } else {
                log.warn("Requested tool '{}' not found in registry", name.trim());
            }
        }
        return filtered;
    }
}
