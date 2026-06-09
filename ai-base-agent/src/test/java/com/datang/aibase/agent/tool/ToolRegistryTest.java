package com.datang.aibase.agent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolRegistryTest {

    private Tool toolA;
    private Tool toolB;
    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        toolA = mock(Tool.class);
        when(toolA.getName()).thenReturn("tool_a");
        toolB = mock(Tool.class);
        when(toolB.getName()).thenReturn("tool_b");
        registry = new ToolRegistry(List.of(toolA, toolB));
    }

    @Test
    @DisplayName("get returns registered tool by name")
    void get_returnsTool() {
        assertThat(registry.get("tool_a")).isSameAs(toolA);
        assertThat(registry.get("tool_b")).isSameAs(toolB);
    }

    @Test
    @DisplayName("get returns null for unknown tool")
    void get_unknown_returnsNull() {
        assertThat(registry.get("unknown")).isNull();
    }

    @Test
    @DisplayName("getToolNames returns all registered tool names")
    void getToolNames_returnsAllNames() {
        assertThat(registry.getToolNames()).containsExactlyInAnyOrder("tool_a", "tool_b");
    }

    @Test
    @DisplayName("getAll returns unmodifiable copy of all tools")
    void getAll_returnsAllTools() {
        Map<String, Tool> all = registry.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get("tool_a")).isSameAs(toolA);
    }

    @Test
    @DisplayName("getFiltered returns only allowed tools")
    void getFiltered_returnsFilteredTools() {
        Map<String, Tool> filtered = registry.getFiltered(Set.of("tool_a"));

        assertThat(filtered).hasSize(1);
        assertThat(filtered).containsKey("tool_a");
    }

    @Test
    @DisplayName("getFiltered with null returns all tools")
    void getFiltered_null_returnsAll() {
        Map<String, Tool> filtered = registry.getFiltered(null);

        assertThat(filtered).hasSize(2);
    }

    @Test
    @DisplayName("getFiltered with empty set returns all tools")
    void getFiltered_empty_returnsAll() {
        Map<String, Tool> filtered = registry.getFiltered(Set.of());

        assertThat(filtered).hasSize(2);
    }

    @Test
    @DisplayName("getFiltered skips unknown tool names")
    void getFiltered_unknownName_skipped() {
        Map<String, Tool> filtered = registry.getFiltered(Set.of("tool_a", "nonexistent"));

        assertThat(filtered).hasSize(1);
        assertThat(filtered).containsKey("tool_a");
    }
}
