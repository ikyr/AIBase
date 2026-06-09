package com.datang.aibase.skill.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.skill.client.ModelGatewayClient;
import com.datang.aibase.skill.engine.GraalJsSandbox;
import com.datang.aibase.skill.entity.SkillDef;
import com.datang.aibase.skill.entity.SkillExecutionLog;
import com.datang.aibase.skill.entity.SkillVersion;
import com.datang.aibase.skill.mapper.SkillDefMapper;
import com.datang.aibase.skill.mapper.SkillExecutionLogMapper;
import com.datang.aibase.skill.mapper.SkillVersionMapper;
import com.datang.aibase.skill.service.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkillServiceImpl implements SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillServiceImpl.class);

    private final SkillDefMapper skillDefMapper;
    private final SkillVersionMapper skillVersionMapper;
    private final SkillExecutionLogMapper executionLogMapper;
    private final ModelGatewayClient modelGatewayClient;
    private final GraalJsSandbox sandbox;
    private final RestClient agentClient;

    public SkillServiceImpl(SkillDefMapper skillDefMapper,
                            SkillVersionMapper skillVersionMapper,
                            SkillExecutionLogMapper executionLogMapper,
                            ModelGatewayClient modelGatewayClient,
                            GraalJsSandbox sandbox,
                            @Value("${agent.url:http://localhost:8105}") String agentUrl) {
        this.skillDefMapper = skillDefMapper;
        this.skillVersionMapper = skillVersionMapper;
        this.executionLogMapper = executionLogMapper;
        this.modelGatewayClient = modelGatewayClient;
        this.sandbox = sandbox;
        this.agentClient = RestClient.create(agentUrl);
    }

    @Override
    public List<SkillDef> listAll() {
        return skillDefMapper.selectAll();
    }

    @Override
    @Cacheable(value = "skill_def", key = "#id")
    public SkillDef getById(String id) {
        return skillDefMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "skill_def", key = "#skill.id")
    public SkillDef create(SkillDef skill) {
        skill.setId(SnowflakeIdGenerator.nextId());
        skillDefMapper.insert(skill);
        return skill;
    }

    @Override
    public List<SkillVersion> getVersions(String skillId) {
        return skillVersionMapper.selectBySkillId(skillId);
    }

    @Override
    public SkillVersion addVersion(SkillVersion version) {
        version.setId(SnowflakeIdGenerator.nextId());
        skillVersionMapper.insert(version);
        return version;
    }

    @Override
    public List<SkillExecutionLog> listExecutionLogs(int limit) {
        return executionLogMapper.selectRecent(limit);
    }

    @Override
    public List<SkillDef> discover(String query) {
        return skillDefMapper.search(query);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> request) {
        String skillId = (String) request.getOrDefault("skillId", "");
        SkillDef skill = skillDefMapper.selectById(skillId);
        if (skill == null) {
            return Map.of("error", "Skill not found: " + skillId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", Map.of());

        String level = skill.getSkillLevel() != null ? skill.getSkillLevel().toUpperCase() : "PROMPT";
        long start = System.currentTimeMillis();
        Map<String, Object> result;
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            result = switch (level) {
                case "PROMPT" -> executePromptSkill(skill, context);
                case "FUNCTION" -> executeFunctionSkill(skill, context);
                case "AGENT" -> executeAgentSkill(skill, context);
                default -> Map.of("error", "Unknown skill level: " + level);
            };
        } catch (Exception e) {
            log.error("Skill execution failed: skillId={}, level={}", skillId, level, e);
            result = Map.of("error", e.getMessage());
            status = "FAILED";
            errorMsg = e.getMessage();
        }

        long duration = System.currentTimeMillis() - start;
        recordExecutionLog(skillId, context, result, status, errorMsg, (int) duration);
        return result;
    }

    private Map<String, Object> executePromptSkill(SkillDef skill, Map<String, Object> context) {
        String promptTemplate = skill.getPromptTemplate();
        if (promptTemplate == null || promptTemplate.isBlank()) {
            return Map.of("error", "Prompt template is empty");
        }

        String rendered = renderTemplate(promptTemplate, context);
        String response = modelGatewayClient.chat("You are a helpful assistant. Respond based on the given prompt.", rendered);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("skillId", skill.getId());
        result.put("skillName", skill.getName());
        result.put("skillLevel", "PROMPT");
        result.put("output", response);
        return result;
    }

    private Map<String, Object> executeFunctionSkill(SkillDef skill, Map<String, Object> context) {
        String functionCode = skill.getPromptTemplate();
        int timeoutMs = skill.getTimeoutMs() != null ? skill.getTimeoutMs() : 10_000;

        if (functionCode != null && !functionCode.isBlank() && sandbox.isAvailable()) {
            try {
                Map<String, Object> sandboxResult = sandbox.execute(functionCode, context, timeoutMs);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("skillId", skill.getId());
                result.put("skillName", skill.getName());
                result.put("skillLevel", "FUNCTION");
                result.put("engine", "graaljs");
                result.put("output", sandboxResult);
                return result;
            } catch (Exception e) {
                log.warn("GraalJS execution failed for skill {}, falling back to LLM: {}", skill.getId(), e.getMessage());
            }
        }

        return executeFunctionSkillViaLlm(skill, context);
    }

    private Map<String, Object> executeFunctionSkillViaLlm(SkillDef skill, Map<String, Object> context) {
        String inputSchema = skill.getInputSchema();
        String functionDesc = skill.getDescription() != null ? skill.getDescription() : skill.getName();
        String params = skill.getParams() != null ? skill.getParams() : "";

        String systemPrompt = "You are a function executor. Execute the described function with the given input and return ONLY the result as JSON. Function: " + functionDesc + ". Parameters schema: " + params + ". Input schema: " + (inputSchema != null ? inputSchema : "{}");
        String userInput = context.toString();
        String response = modelGatewayClient.chat(systemPrompt, userInput);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("skillId", skill.getId());
        result.put("skillName", skill.getName());
        result.put("skillLevel", "FUNCTION");
        result.put("engine", "llm");
        result.put("output", response);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeAgentSkill(SkillDef skill, Map<String, Object> context) {
        String agentRefId = skill.getAgentRefId();
        if (agentRefId == null || agentRefId.isBlank()) {
            return Map.of("error", "Agent reference ID is empty");
        }

        String message = context.getOrDefault("message", context.toString()).toString();
        Map<String, Object> agentBody = Map.of("message", message);

        try {
            Map<String, Object> response = agentClient.post()
                    .uri("/api/v1/agent/{agentId}/chat", agentRefId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(agentBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Map.of("error", "Empty response from agent service");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("skillId", skill.getId());
            result.put("skillName", skill.getName());
            result.put("skillLevel", "AGENT");
            result.put("agentRefId", agentRefId);

            Object data = response.get("data");
            if (data instanceof Map<?, ?> dm) {
                result.put("output", ((Map<String, Object>) dm).getOrDefault("content", ""));
            } else {
                result.put("output", response.toString());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Agent skill delegation failed: " + e.getMessage(), e);
        }
    }

    private String renderTemplate(String template, Map<String, Object> context) {
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    private void recordExecutionLog(String skillId, Map<String, Object> input, Map<String, Object> output,
                                     String status, String errorMsg, int durationMs) {
        try {
            SkillExecutionLog execLog = new SkillExecutionLog();
            execLog.setId(SnowflakeIdGenerator.nextId());
            execLog.setSkillId(skillId);
            execLog.setInput(input.toString());
            execLog.setOutput(output.toString());
            execLog.setStatus(status);
            execLog.setDurationMs(durationMs);
            execLog.setErrorMsg(errorMsg);
            execLog.setCreatedAt(LocalDateTime.now());
            execLog.setUpdatedAt(LocalDateTime.now());
            executionLogMapper.insert(execLog);
        } catch (Exception e) {
            log.warn("Failed to record execution log: {}", e.getMessage());
        }
    }
}
