package com.datang.aibase.workflow.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowExecutorTest_Expression {

    @Test
    @DisplayName("evaluateExpression: field == value returns value when match")
    void eq_match_returnsValue() {
        var executor = createExecutor();

        String result = executor.evaluateExpression("status == 'active'",
                Map.of("status", "active"));

        assertThat(result).isEqualTo("active");
    }

    @Test
    @DisplayName("evaluateExpression: field == value returns false when no match")
    void eq_noMatch_returnsFalse() {
        var executor = createExecutor();

        String result = executor.evaluateExpression("status == 'active'",
                Map.of("status", "inactive"));

        assertThat(result).isEqualTo("false");
    }

    @Test
    @DisplayName("evaluateExpression: field != value returns true when different")
    void neq_returnsTrue() {
        var executor = createExecutor();

        String result = executor.evaluateExpression("status != 'blocked'",
                Map.of("status", "active"));

        assertThat(result).isEqualTo("true");
    }

    @Test
    @DisplayName("evaluateExpression: simple truthy check")
    void truthy_returnsTrue() {
        var executor = createExecutor();

        String result = executor.evaluateExpression("flag", Map.of("flag", "yes"));

        assertThat(result).isEqualTo("true");
    }

    @Test
    @DisplayName("evaluateExpression: null or blank returns default")
    void nullOrBlank_returnsDefault() {
        var executor = createExecutor();

        assertThat(executor.evaluateExpression(null, Map.of())).isEqualTo("default");
        assertThat(executor.evaluateExpression("", Map.of())).isEqualTo("default");
    }

    @Test
    @DisplayName("topologicalSort: linear DAG returns correct order")
    void topologicalSort_linear_returnsOrdered() {
        var executor = createExecutor();
        var nodes = List.of(
            new DagParser.ParsedNode("n1", "Start", "START", null, null, Map.of()),
            new DagParser.ParsedNode("n2", "Middle", "SKILL", "s1", null, Map.of()),
            new DagParser.ParsedNode("n3", "End", "END", null, null, Map.of())
        );
        var edges = List.of(
            new DagParser.ParsedEdge("n1", "n2", null),
            new DagParser.ParsedEdge("n2", "n3", null)
        );

        var sorted = executor.topologicalSort(nodes, edges);

        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).id()).isEqualTo("n1");
        assertThat(sorted.get(2).id()).isEqualTo("n3");
    }

    @Test
    @DisplayName("topologicalSort: empty nodes returns empty")
    void topologicalSort_empty_returnsEmpty() {
        var executor = createExecutor();

        assertThat(executor.topologicalSort(List.of(), List.of())).isEmpty();
    }

    private WorkflowExecutor createExecutor() {
        return new WorkflowExecutor(null, null,
                "http://localhost:8102", "http://localhost:8105", "http://localhost:8101");
    }
}
