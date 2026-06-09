package com.datang.aibase.workflow.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DagParserTest {

    private final DagParser parser = new DagParser();

    @Test
    @DisplayName("parseNodes returns empty list for null")
    void parseNodes_null_returnsEmpty() {
        assertThat(parser.parseNodes(null)).isEmpty();
    }

    @Test
    @DisplayName("parseNodes returns empty list for blank")
    void parseNodes_blank_returnsEmpty() {
        assertThat(parser.parseNodes("   ")).isEmpty();
    }

    @Test
    @DisplayName("parseNodes parses steps array")
    void parseNodes_stepsArray() {
        String json = "{\"steps\":[{\"id\":\"n1\",\"name\":\"Start\",\"type\":\"START\"}]}";

        var nodes = parser.parseNodes(json);

        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0).id()).isEqualTo("n1");
    }

    @Test
    @DisplayName("parseNodes parses nodes array as fallback")
    void parseNodes_nodesArray() {
        String json = "{\"nodes\":[{\"id\":\"n1\",\"name\":\"Start\",\"type\":\"START\"}]}";

        var nodes = parser.parseNodes(json);

        assertThat(nodes).hasSize(1);
    }

    @Test
    @DisplayName("parseEdges returns empty for null")
    void parseEdges_null_returnsEmpty() {
        assertThat(parser.parseEdges(null)).isEmpty();
    }

    @Test
    @DisplayName("parseEdges parses edges array")
    void parseEdges_parsesCorrectly() {
        String json = "{\"edges\":[{\"from\":\"n1\",\"to\":\"n2\",\"label\":\"yes\"}]}";

        var edges = parser.parseEdges(json);

        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).from()).isEqualTo("n1");
        assertThat(edges.get(0).to()).isEqualTo("n2");
        assertThat(edges.get(0).label()).isEqualTo("yes");
    }

    @Test
    @DisplayName("countNodes returns 0 for blank json")
    void countNodes_blank_returnsZero() {
        assertThat(parser.countNodes("")).isEqualTo(0);
    }

    @Test
    @DisplayName("countNodes counts correctly")
    void countNodes_counts() {
        String json = "{\"steps\":[{\"id\":\"a\"},{\"id\":\"b\"},{\"id\":\"c\"}]}";

        assertThat(parser.countNodes(json)).isEqualTo(3);
    }
}
