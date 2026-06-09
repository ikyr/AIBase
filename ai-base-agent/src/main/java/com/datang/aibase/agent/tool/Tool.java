package com.datang.aibase.agent.tool;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    String getInputSchema();
    Map<String, Object> execute(Map<String, Object> input);
}
