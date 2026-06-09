package com.datang.aibase.workflow.engine;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.workflow.client.ModelGatewayClient;
import com.datang.aibase.workflow.entity.WfDefinition;
import com.datang.aibase.workflow.entity.WfInstance;
import com.datang.aibase.workflow.entity.WfNodeExec;
import com.datang.aibase.workflow.mapper.WfNodeExecMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Component
public class WorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private final WfNodeExecMapper nodeExecMapper;
    private final ModelGatewayClient modelGatewayClient;
    private final RestClient skillClient;
    private final RestClient agentClient;
    private final RestClient knowledgeClient;

    public WorkflowExecutor(WfNodeExecMapper nodeExecMapper,
                            ModelGatewayClient modelGatewayClient,
                            @Value("${skill-service.url:http://localhost:8102}") String skillBaseUrl,
                            @Value("${agent-service.url:http://localhost:8105}") String agentBaseUrl,
                            @Value("${knowledge-service.url:http://localhost:8101}") String knowledgeBaseUrl) {
        this.nodeExecMapper = nodeExecMapper;
        this.modelGatewayClient = modelGatewayClient;
        this.skillClient = RestClient.create(skillBaseUrl);
        this.agentClient = RestClient.create(agentBaseUrl);
        this.knowledgeClient = RestClient.create(knowledgeBaseUrl);
    }

    public void execute(WfInstance instance, WfDefinition definition, Map<String, Object> input) {
        instance.setStatus("RUNNING");
        instance.setStartedAt(LocalDateTime.now());

        Map<String, Object> context = new LinkedHashMap<>(input != null ? input : Map.of());
        context.put("instanceId", instance.getId());
        context.put("definitionId", definition.getId());

        try {
            List<DagParser.ParsedNode> nodes = parseDagNodes(definition.getDag());
            List<DagParser.ParsedEdge> edges = parseDagEdges(definition.getDag());
            List<DagParser.ParsedNode> sorted = topologicalSort(nodes, edges);

            if (sorted.isEmpty()) {
                finishInstance(instance, "COMPLETED", mapper.writeValueAsString(Map.of("result", "Empty DAG")), null);
                return;
            }

            int maxRetries = parseMaxRetries(definition.getRetryPolicy());
            Set<String> blockedNodeIds = new HashSet<>();

            for (DagParser.ParsedNode node : sorted) {
                if (blockedNodeIds.contains(node.id())) {
                    log.info("Skipping blocked node {} ({})", node.name(), node.id());
                    continue;
                }

                if ("WAIT".equalsIgnoreCase(node.type()) && "RESUMING".equals(instance.getStatus())) {
                    instance.setStatus("RUNNING");
                }

                log.info("Executing node {} ({}) of type {}", node.name(), node.id(), node.type());
                context.put("currentNode", node.id());
                context.put("currentNodeName", node.name());

                WfNodeExec nodeExec = createNodeExec(instance.getId(), node, context);
                nodeExecMapper.insert(nodeExec);

                Map<String, Object> stepResult = executeNodeWithRetry(node, context, maxRetries, nodeExec);

                if (isError(stepResult)) {
                    nodeExec.setStatus("FAILED");
                    nodeExec.setError(String.valueOf(stepResult.get("error")));
                    nodeExec.setFinishedAt(LocalDateTime.now());
                    nodeExecMapper.update(nodeExec);

                    context.put("errorNode", node.id());
                    context.put("errorNodeName", node.name());
                    context.put("error", stepResult.get("error"));
                    finishInstance(instance, "FAILED",
                            mapper.writeValueAsString(context),
                            "Failed at node " + node.name() + ": " + stepResult.get("error"));
                    return;
                }

                nodeExec.setStatus("COMPLETED");
                nodeExec.setOutput(toJson(stepResult));
                nodeExec.setFinishedAt(LocalDateTime.now());
                nodeExecMapper.update(nodeExec);

                context.put("lastResult", stepResult);

                if ("WAIT".equalsIgnoreCase(node.type())) {
                    instance.setStatus("PAUSED");
                    instance.setOutput(mapper.writeValueAsString(context));
                    instance.setCompletedAt(null);
                    return;
                }

                if ("CONDITION".equalsIgnoreCase(node.type())) {
                    String branch = String.valueOf(stepResult.getOrDefault("branch", ""));
                    blockedNodeIds.addAll(computeBlockedNodes(node.id(), branch, nodes, edges));
                }

                log.info("Node {} completed", node.name());
            }

            finishInstance(instance, "COMPLETED", mapper.writeValueAsString(context), null);
        } catch (Exception e) {
            log.error("Workflow execution failed: {}", e.getMessage(), e);
            finishInstance(instance, "FAILED", null, e.getMessage());
        }
    }

    public void resume(WfInstance instance, WfDefinition definition, Map<String, Object> signalData) {
        instance.setStatus("RESUMING");
        Map<String, Object> context = parseContext(instance.getOutput());
        if (signalData != null) {
            context.putAll(signalData);
        }
        context.put("_resumed", true);
        execute(instance, definition, context);
    }

    // ---- Node execution ----

    private Map<String, Object> executeNode(DagParser.ParsedNode node, Map<String, Object> context) {
        return switch (node.type() != null ? node.type().toUpperCase() : "") {
            case "START" -> Map.of("started", true);
            case "END" -> Map.of("completed", true);
            case "SKILL" -> executeSkillNode(node, context);
            case "AGENT" -> executeAgentNode(node, context);
            case "LLM_CALL" -> executeLlmCallNode(node, context);
            case "CONDITION" -> executeConditionNode(node, context);
            case "PARALLEL" -> executeParallelNode(node, context);
            case "CODE" -> executeCodeNode(node, context);
            case "WAIT" -> executeWaitNode(node, context);
            case "KNOWLEDGE" -> executeKnowledgeNode(node, context);
            default -> Map.of("node", node.name(), "type", node.type(), "status", "executed");
        };
    }

    // ---- Retry logic ----

    private Map<String, Object> executeNodeWithRetry(DagParser.ParsedNode node, Map<String, Object> context,
                                                      int maxRetries, WfNodeExec nodeExec) {
        int attempt = 0;
        Map<String, Object> result = null;
        long backoffMs = 1000;

        while (attempt <= maxRetries) {
            try {
                result = executeNode(node, context);
                if (!isError(result)) return result;

                if (attempt < maxRetries) {
                    log.warn("Node {} attempt {}/{} failed: {}, retrying in {}ms",
                            node.name(), attempt + 1, maxRetries + 1, result.get("error"), backoffMs);
                    nodeExec.setStatus("RETRYING");
                    nodeExecMapper.update(nodeExec);
                    sleep(backoffMs);
                    backoffMs *= 2;
                }
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn("Node {} attempt {}/{} exception: {}, retrying in {}ms",
                            node.name(), attempt + 1, maxRetries + 1, e.getMessage(), backoffMs);
                    sleep(backoffMs);
                    backoffMs *= 2;
                } else {
                    result = Map.of("error", e.getMessage());
                }
            }
            attempt++;
        }
        return result != null ? result : Map.of("error", "Max retries exceeded");
    }

    // ---- LLM_CALL ----

    private Map<String, Object> executeLlmCallNode(DagParser.ParsedNode node, Map<String, Object> context) {
        Map<String, Object> config = node.config() != null ? node.config() : Map.of();
        String prompt = resolveTemplate(String.valueOf(config.getOrDefault("prompt", "Hello")), context);
        String model = String.valueOf(config.getOrDefault("model", "qwen-plus"));

        String result = modelGatewayClient.chat(prompt, model);
        return Map.of("content", result, "model", model);
    }

    // ---- CONDITION ----

    private Map<String, Object> executeConditionNode(DagParser.ParsedNode node, Map<String, Object> context) {
        Map<String, Object> config = node.config() != null ? node.config() : Map.of();
        String expr = String.valueOf(config.getOrDefault("expr", "true"));
        String branch = evaluateExpression(expr, context);
        log.info("Condition {} evaluated: '{}' -> branch '{}'", node.name(), expr, branch);
        return Map.of("branch", branch, "expression", expr);
    }

    String evaluateExpression(String expr, Map<String, Object> context) {
        if (expr == null || expr.isBlank()) return "default";

        // Try: field == 'value' or field != 'value'
        String[] parts = expr.split("\\s*==\\s*");
        if (parts.length == 2) {
            Object fieldValue = resolveValue(parts[0].trim(), context);
            String expected = stripQuotes(parts[1].trim());
            return String.valueOf(fieldValue).equals(expected) ? expected : "false";
        }

        parts = expr.split("\\s*!=\\s*");
        if (parts.length == 2) {
            Object fieldValue = resolveValue(parts[0].trim(), context);
            String expected = stripQuotes(parts[1].trim());
            return !String.valueOf(fieldValue).equals(expected) ? "true" : expected;
        }

        // Simple truthy check
        Object val = resolveValue(expr.trim(), context);
        return val != null && !"false".equals(String.valueOf(val)) ? "true" : "false";
    }

    private Object resolveValue(String field, Map<String, Object> context) {
        // Handle nested: a.b.c
        String[] path = field.split("\\.");
        Object current = context;
        for (String key : path) {
            if (current instanceof Map<?, ?> m) {
                current = m.get(key);
            } else {
                return field; // literal
            }
        }
        return current != null ? current : field;
    }

    private Set<String> computeBlockedNodes(String conditionNodeId, String branch,
                                             List<DagParser.ParsedNode> nodes,
                                             List<DagParser.ParsedEdge> edges) {
        Set<String> blocked = new HashSet<>();
        Map<String, String> nodeIdToName = new LinkedHashMap<>();
        for (var n : nodes) nodeIdToName.put(n.id(), n.id());

        for (var edge : edges) {
            if (edge.from().equals(conditionNodeId)) {
                String edgeLabel = edge.label() != null ? edge.label() : "";
                if (!edgeLabel.equals(branch)) {
                    collectReachable(edge.to(), edges, nodeIdToName, blocked);
                }
            }
        }
        return blocked;
    }

    private void collectReachable(String startId, List<DagParser.ParsedEdge> edges,
                                   Map<String, String> nodeIds, Set<String> collected) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startId);
        while (!queue.isEmpty()) {
            String id = queue.poll();
            if (collected.add(id)) {
                for (var edge : edges) {
                    if (edge.from().equals(id) && nodeIds.containsKey(edge.to())) {
                        queue.add(edge.to());
                    }
                }
            }
        }
    }

    // ---- PARALLEL ----

    private Map<String, Object> executeParallelNode(DagParser.ParsedNode node, Map<String, Object> context) {
        List<Map<String, Object>> children = parseChildren(node);
        if (children.isEmpty()) {
            return Map.of("results", List.of(), "status", "no_children");
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            List<CompletableFuture<Map<String, Object>>> futures = children.stream()
                    .map(child -> CompletableFuture.supplyAsync(() -> {
                        DagParser.ParsedNode childNode = mapper.convertValue(child, DagParser.ParsedNode.class);
                        return executeNode(childNode, context);
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<Map<String, Object>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            Map<String, Object> merged = new LinkedHashMap<>();
            for (int i = 0; i < results.size(); i++) {
                merged.put("child_" + i, results.get(i));
            }
            merged.put("totalChildren", results.size());
            return merged;
        } finally {
            executor.shutdownNow();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseChildren(DagParser.ParsedNode node) {
        if (node.config() != null && node.config().get("children") instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }

    // ---- CODE ----

    private Map<String, Object> executeCodeNode(DagParser.ParsedNode node, Map<String, Object> context) {
        Map<String, Object> config = node.config() != null ? node.config() : Map.of();
        String code = String.valueOf(config.getOrDefault("code", "''"));
        int timeoutMs = config.get("timeout") instanceof Number n ? n.intValue() : 10000;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Map<String, Object>> future = executor.submit(() -> {
                var engine = scriptEngineManager.getEngineByName("JavaScript");
                if (engine == null) {
                    return Map.<String, Object>of("error", "JavaScript engine not available");
                }
                engine.put("ctx", context);
                Object result = engine.eval(code);
                return Map.of("result", result != null ? result.toString() : "null");
            });

            try {
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                return Map.of("error", "Code execution timed out after " + timeoutMs + "ms");
            }
        } catch (Exception e) {
            return Map.of("error", "Code execution failed: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    // ---- WAIT ----

    private Map<String, Object> executeWaitNode(DagParser.ParsedNode node, Map<String, Object> context) {
        Map<String, Object> config = node.config() != null ? node.config() : Map.of();
        String signalName = String.valueOf(config.getOrDefault("signal", node.id()));
        String title = String.valueOf(config.getOrDefault("title", node.name()));
        return Map.of("status", "waiting", "signal", signalName, "title", title);
    }

    // ---- KNOWLEDGE ----

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeKnowledgeNode(DagParser.ParsedNode node, Map<String, Object> context) {
        Map<String, Object> config = node.config() != null ? node.config() : Map.of();
        String query = resolveTemplate(String.valueOf(config.getOrDefault("query", "")), context);
        String kbId = String.valueOf(config.getOrDefault("kbId", ""));
        int topK = config.get("topK") instanceof Number n ? n.intValue() : 5;

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("query", query);
        requestBody.put("kbId", kbId);
        requestBody.put("topK", topK);

        try {
            Map<String, Object> response = knowledgeClient.post()
                    .uri("/api/v1/knowledge/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                return (Map<String, Object>) data;
            }
            return Map.of("error", "Knowledge search returned unexpected response");
        } catch (Exception e) {
            log.error("Knowledge search failed: {}", e.getMessage());
            return Map.of("error", "Knowledge search failed: " + e.getMessage());
        }
    }

    // ---- Existing node types (SKILL, AGENT) ----

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeSkillNode(DagParser.ParsedNode node, Map<String, Object> context) {
        String skillId = node.refId();
        if (skillId == null || skillId.isBlank()) {
            return Map.of("error", "Skill node missing refId");
        }
        Map<String, Object> requestBody = Map.of("skillId", skillId, "context", context);
        try {
            Map<String, Object> response = skillClient.post()
                    .uri("/api/v1/skill/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                return (Map<String, Object>) data;
            }
            return Map.of("error", "Skill execution returned unexpected response");
        } catch (Exception e) {
            log.error("Skill execution failed for node {}: {}", node.name(), e.getMessage());
            return Map.of("error", "Skill execution failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeAgentNode(DagParser.ParsedNode node, Map<String, Object> context) {
        String agentId = node.refId();
        if (agentId == null || agentId.isBlank()) {
            return Map.of("error", "Agent node missing refId");
        }
        String message = String.valueOf(context.getOrDefault("message",
                context.getOrDefault("input", "Execute agent task")));
        Map<String, Object> requestBody = Map.of("message", message);
        try {
            Map<String, Object> response = agentClient.post()
                    .uri("/api/v1/agent/{agentId}/chat", agentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("success") instanceof Boolean b && b
                    && response.get("data") instanceof Map<?, ?> data) {
                return (Map<String, Object>) data;
            }
            return Map.of("error", "Agent execution returned unexpected response");
        } catch (Exception e) {
            log.error("Agent execution failed for node {}: {}", node.name(), e.getMessage());
            return Map.of("error", "Agent execution failed: " + e.getMessage());
        }
    }

    // ---- DAG parsing ----

    private List<DagParser.ParsedNode> parseDagNodes(String dagJson) {
        try {
            Map<String, Object> dag = mapper.readValue(dagJson, new TypeReference<>() {});
            Object nodesObj = dag.get("steps");
            if (nodesObj == null) nodesObj = dag.get("nodes");
            if (nodesObj instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(n -> mapper.convertValue(n, DagParser.ParsedNode.class))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to parse DAG nodes: {}", e.getMessage());
        }
        return List.of();
    }

    private List<DagParser.ParsedEdge> parseDagEdges(String dagJson) {
        try {
            Map<String, Object> dag = mapper.readValue(dagJson, new TypeReference<>() {});
            Object edgesObj = dag.get("edges");
            if (edgesObj instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(e -> mapper.convertValue(e, DagParser.ParsedEdge.class))
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Failed to parse DAG edges: {}", e.getMessage());
        }
        return List.of();
    }

    // ---- Utilities ----

    private WfNodeExec createNodeExec(String instanceId, DagParser.ParsedNode node, Map<String, Object> context) {
        WfNodeExec nodeExec = new WfNodeExec();
        nodeExec.setId(SnowflakeIdGenerator.nextId());
        nodeExec.setWfExecId(instanceId);
        nodeExec.setNodeId(node.id());
        nodeExec.setNodeName(node.name());
        nodeExec.setNodeType(node.type());
        nodeExec.setStatus("RUNNING");
        nodeExec.setInput(toJson(context));
        nodeExec.setStartedAt(LocalDateTime.now());
        return nodeExec;
    }

    List<DagParser.ParsedNode> topologicalSort(List<DagParser.ParsedNode> nodes, List<DagParser.ParsedEdge> edges) {
        if (nodes.isEmpty()) return List.of();

        Map<String, List<String>> adj = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();
        for (var node : nodes) {
            adj.putIfAbsent(node.id(), new ArrayList<>());
            inDegree.putIfAbsent(node.id(), 0);
        }
        for (var edge : edges) {
            if (inDegree.containsKey(edge.to())) {
                adj.computeIfAbsent(edge.from(), k -> new ArrayList<>()).add(edge.to());
                inDegree.merge(edge.to(), 1, Integer::sum);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        Map<String, DagParser.ParsedNode> nodeMap = new LinkedHashMap<>();
        for (var n : nodes) nodeMap.put(n.id(), n);

        List<DagParser.ParsedNode> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String id = queue.poll();
            DagParser.ParsedNode node = nodeMap.get(id);
            if (node != null) sorted.add(node);
            for (String neighbor : adj.getOrDefault(id, List.of())) {
                int deg = inDegree.merge(neighbor, -1, Integer::sum);
                if (deg == 0) queue.add(neighbor);
            }
        }
        return sorted;
    }

    private int parseMaxRetries(String retryPolicyJson) {
        if (retryPolicyJson == null || retryPolicyJson.isBlank()) return 0;
        try {
            Map<String, Object> policy = mapper.readValue(retryPolicyJson, new TypeReference<>() {});
            Object maxRetries = policy.get("maxRetries");
            return maxRetries instanceof Number n ? n.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String resolveTemplate(String template, Map<String, Object> context) {
        if (template == null) return "";
        String result = template;
        for (var entry : context.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseContext(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private boolean isError(Map<String, Object> result) {
        return result != null && result.containsKey("error");
    }

    private void finishInstance(WfInstance instance, String status, String output, String errorMsg) {
        instance.setStatus(status);
        if (output != null) instance.setOutput(output);
        instance.setErrorMsg(errorMsg);
        instance.setCompletedAt(LocalDateTime.now());
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String stripQuotes(String s) {
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
