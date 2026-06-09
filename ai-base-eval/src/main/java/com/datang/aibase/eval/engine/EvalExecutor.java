package com.datang.aibase.eval.engine;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.eval.entity.EvalDatasetItem;
import com.datang.aibase.eval.entity.EvalResult;
import com.datang.aibase.eval.entity.EvalTask;
import com.datang.aibase.eval.mapper.EvalDatasetItemMapper;
import com.datang.aibase.eval.mapper.EvalResultMapper;
import com.datang.aibase.eval.mapper.EvalTaskMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class EvalExecutor {

    private static final Logger log = LoggerFactory.getLogger(EvalExecutor.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final EvalTaskMapper taskMapper;
    private final EvalResultMapper resultMapper;
    private final EvalDatasetItemMapper itemMapper;
    private final RestClient knowledgeClient;
    private final RestClient agentClient;
    private final RestClient skillClient;

    public EvalExecutor(EvalTaskMapper taskMapper,
                        EvalResultMapper resultMapper,
                        EvalDatasetItemMapper itemMapper,
                        @Value("${knowledge.url:http://localhost:8101}") String knowledgeUrl,
                        @Value("${agent.url:http://localhost:8104}") String agentUrl,
                        @Value("${skill.url:http://localhost:8102}") String skillUrl) {
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.itemMapper = itemMapper;
        this.knowledgeClient = RestClient.create(knowledgeUrl);
        this.agentClient = RestClient.create(agentUrl);
        this.skillClient = RestClient.create(skillUrl);
    }

    public void execute(String taskId) {
        EvalTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("Task not found: {}", taskId);
            return;
        }

        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);

        List<EvalDatasetItem> items = itemMapper.selectByDatasetId(task.getDatasetId());
        task.setTotalItems(items.size());
        taskMapper.update(task);

        if (items.isEmpty()) {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            task.setMetrics("{\"successRate\": 0, \"note\": \"No dataset items\"}");
            taskMapper.update(task);
            return;
        }

        int passed = 0;
        List<EvalResult> results = new ArrayList<>();

        for (EvalDatasetItem item : items) {
            try {
                EvalResult result = evaluate(task, item);
                results.add(result);
                resultMapper.insert(result);
                if (Boolean.TRUE.equals(result.getPassed())) {
                    passed++;
                }
            } catch (Exception e) {
                log.error("Evaluation failed for item {}: {}", item.getId(), e.getMessage());
                EvalResult failed = new EvalResult();
                failed.setId(SnowflakeIdGenerator.nextId());
                failed.setTaskId(taskId);
                failed.setItemId(item.getId());
                failed.setPassed(false);
                failed.setErrorMsg(e.getMessage());
                failed.setDurationMs(0);
                resultMapper.insert(failed);
                results.add(failed);
            }
        }

        Map<String, Object> metrics = computeAggregatedMetrics(task, results, items.size(), passed);
        try {
            task.setMetrics(mapper.writeValueAsString(metrics));
        } catch (Exception ignored) {}

        task.setPassedItems(passed);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(task);
    }

    private EvalResult evaluate(EvalTask task, EvalDatasetItem item) {
        long start = System.currentTimeMillis();

        String targetType = task.getTargetType() != null ? task.getTargetType().toUpperCase() : "AGENT";
        Map<String, Object> output = switch (targetType) {
            case "RAG", "KNOWLEDGE" -> evaluateRag(task, item);
            case "SKILL" -> evaluateSkill(task, item);
            default -> evaluateAgent(task, item);
        };

        int durationMs = (int) (System.currentTimeMillis() - start);

        EvalResult result = new EvalResult();
        result.setId(SnowflakeIdGenerator.nextId());
        result.setTaskId(task.getId());
        result.setItemId(item.getId());
        result.setDurationMs(durationMs);

        try {
            result.setActualOutput(mapper.writeValueAsString(output));
        } catch (Exception ignored) {}

        result.setPassed(judge(item, output));

        try {
            Map<String, Object> itemMetrics = new LinkedHashMap<>();
            itemMetrics.put("durationMs", durationMs);
            result.setMetrics(mapper.writeValueAsString(itemMetrics));
        } catch (Exception ignored) {}

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateRag(EvalTask task, EvalDatasetItem item) {
        Map<String, Object> body = Map.of(
                "kbId", task.getTargetId(),
                "query", item.getQuestion(),
                "topK", 10,
                "searchType", "HYBRID"
        );

        try {
            Map<String, Object> response = knowledgeClient.post()
                    .uri("/api/v1/knowledge/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof List<?> list) {
                return Map.of("items", list);
            }
            return Map.of("items", List.of());
        } catch (Exception e) {
            log.warn("RAG eval failed for item {}: {}", item.getId(), e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateAgent(EvalTask task, EvalDatasetItem item) {
        Map<String, Object> body = Map.of(
                "sessionId", (Object) null,
                "message", item.getQuestion()
        );

        try {
            Map<String, Object> response = agentClient.post()
                    .uri("/api/v1/agent/{id}/chat", task.getTargetId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof Map<?, ?> dm) {
                return (Map<String, Object>) dm;
            }
            return Map.of("output", response != null ? response.toString() : "");
        } catch (Exception e) {
            log.warn("Agent eval failed for item {}: {}", item.getId(), e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateSkill(EvalTask task, EvalDatasetItem item) {
        Map<String, Object> context;
        try {
            context = mapper.readValue(item.getContext() != null ? item.getContext() : "{}",
                    new TypeReference<>() {});
        } catch (Exception e) {
            context = Map.of("query", item.getQuestion());
        }

        Map<String, Object> body = Map.of(
                "skillId", task.getTargetId(),
                "context", context
        );

        try {
            Map<String, Object> response = skillClient.post()
                    .uri("/api/v1/skill/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.get("data") instanceof Map<?, ?> dm) {
                return (Map<String, Object>) dm;
            }
            return Map.of("output", response != null ? response.toString() : "");
        } catch (Exception e) {
            log.warn("Skill eval failed for item {}: {}", item.getId(), e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private boolean judge(EvalDatasetItem item, Map<String, Object> output) {
        String expected = item.getExpectedAnswer();
        if (expected == null || expected.isBlank()) return true;

        try {
            String actualStr = mapper.writeValueAsString(output);
            return actualStr.toLowerCase().contains(expected.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> computeAggregatedMetrics(EvalTask task, List<EvalResult> results, int total, int passed) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalItems", total);
        metrics.put("passedItems", passed);
        metrics.put("successRate", MetricCalculator.successRate(passed, total));

        double avgDuration = results.stream()
                .filter(r -> r.getDurationMs() != null)
                .mapToInt(EvalResult::getDurationMs)
                .average()
                .orElse(0.0);
        metrics.put("avgDurationMs", Math.round(avgDuration));

        return metrics;
    }
}
