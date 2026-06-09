package com.datang.aibase.workflow.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DagParser {

    private static final Logger log = LoggerFactory.getLogger(DagParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public record ParsedNode(String id, String name, String type, String refId, String refName,
                             java.util.Map<String, Object> config) {}
    public record ParsedEdge(String from, String to, String label) {}

    public List<ParsedNode> parseNodes(String dagJson) {
        if (dagJson == null || dagJson.isBlank()) return List.of();
        try {
            Map<String, Object> dag = mapper.readValue(dagJson, new TypeReference<>() {});
            Object nodesObj = dag.get("steps");
            if (nodesObj == null) nodesObj = dag.get("nodes");
            if (nodesObj instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(n -> mapper.convertValue(n, ParsedNode.class))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to parse DAG nodes: {}", e.getMessage());
        }
        return List.of();
    }

    public List<ParsedEdge> parseEdges(String dagJson) {
        if (dagJson == null || dagJson.isBlank()) return List.of();
        try {
            Map<String, Object> dag = mapper.readValue(dagJson, new TypeReference<>() {});
            Object edgesObj = dag.get("edges");
            if (edgesObj instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(e -> mapper.convertValue(e, ParsedEdge.class))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to parse DAG edges: {}", e.getMessage());
        }
        return List.of();
    }

    public int countNodes(String dagJson) {
        return parseNodes(dagJson).size();
    }
}
