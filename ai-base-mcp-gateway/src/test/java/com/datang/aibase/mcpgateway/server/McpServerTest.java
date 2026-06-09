package com.datang.aibase.mcpgateway.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalToolRegistryTest {

    private ExportableTool toolA;
    private ExportableTool toolB;
    private LocalToolRegistry registry;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        toolA = mock(ExportableTool.class);
        when(toolA.getName()).thenReturn("tool.a");
        when(toolA.getDescription()).thenReturn("Tool A");
        when(toolA.getInputSchema()).thenReturn(Map.of("type", "object"));

        toolB = mock(ExportableTool.class);
        when(toolB.getName()).thenReturn("tool.b");
        when(toolB.getDescription()).thenReturn("Tool B");
        when(toolB.getInputSchema()).thenReturn(Map.of("type", "object"));

        ObjectProvider<ExportableTool> provider = mock(ObjectProvider.class);
        doAnswer(inv -> {
            var consumer = (java.util.function.Consumer<ExportableTool>) inv.getArgument(0);
            consumer.accept(toolA);
            consumer.accept(toolB);
            return null;
        }).when(provider).forEach(any());

        registry = new LocalToolRegistry(provider);
    }

    @Test
    @DisplayName("get returns tool by name")
    void get_returnsTool() {
        assertThat(registry.get("tool.a")).isSameAs(toolA);
    }

    @Test
    @DisplayName("get returns null for unknown tool")
    void get_unknown_returnsNull() {
        assertThat(registry.get("unknown")).isNull();
    }

    @Test
    @DisplayName("listAll returns all registered tools")
    void listAll_returnsAll() {
        var tools = registry.listAll();

        assertThat(tools).hasSize(2);
    }
}

class McpServerTransportTest {

    private LocalToolRegistry toolRegistry;
    private McpServerTransport transport;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ExportableTool tool = mock(ExportableTool.class);
        when(tool.getName()).thenReturn("test.tool");
        when(tool.getDescription()).thenReturn("Test tool");
        when(tool.getInputSchema()).thenReturn(Map.of("type", "object"));

        ObjectProvider<ExportableTool> provider = mock(ObjectProvider.class);
        doAnswer(inv -> {
            var consumer = (java.util.function.Consumer<ExportableTool>) inv.getArgument(0);
            consumer.accept(tool);
            return null;
        }).when(provider).forEach(any());

        toolRegistry = new LocalToolRegistry(provider);
        transport = new McpServerTransport(toolRegistry);
    }

    @Test
    @DisplayName("createSession returns SseEmitter with endpoint event")
    void createSession_returnsEmitter() {
        SseEmitter emitter = transport.createSession("test-session");

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("processMessage initialize returns protocol capabilities")
    void processMessage_initialize_returnsCapabilities() {
        Map<String, Object> request = Map.of("method", "initialize", "id", "1");

        Map<String, Object> result = transport.processMessage("test-session", request);

        assertThat(result).containsKey("result");
        @SuppressWarnings("unchecked")
        Map<String, Object> initResult = (Map<String, Object>) result.get("result");
        assertThat(initResult.get("protocolVersion")).isEqualTo("2024-11-05");
    }

    @Test
    @DisplayName("processMessage tools/list returns tool list")
    void processMessage_toolsList_returnsTools() {
        Map<String, Object> request = Map.of("method", "tools/list", "id", "2");

        Map<String, Object> result = transport.processMessage("test-session", request);

        assertThat(result).containsKey("result");
    }

    @Test
    @DisplayName("processMessage unknown method returns error")
    void processMessage_unknownMethod_returnsError() {
        Map<String, Object> request = Map.of("method", "unknown", "id", "3");

        Map<String, Object> result = transport.processMessage("test-session", request);

        assertThat(result).containsKey("error");
    }
}
