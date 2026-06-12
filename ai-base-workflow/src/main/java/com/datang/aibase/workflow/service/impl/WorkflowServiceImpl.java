package com.datang.aibase.workflow.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.workflow.engine.DagParser;
import com.datang.aibase.workflow.engine.WorkflowExecutor;
import com.datang.aibase.workflow.entity.WfDefinition;
import com.datang.aibase.workflow.entity.WfInstance;
import com.datang.aibase.workflow.mapper.WfDefinitionMapper;
import com.datang.aibase.workflow.mapper.WfInstanceMapper;
import com.datang.aibase.workflow.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private final WfDefinitionMapper definitionMapper;
    private final WfInstanceMapper instanceMapper;
    private final DagParser dagParser;
    private final WorkflowExecutor executor;

    public WorkflowServiceImpl(WfDefinitionMapper definitionMapper,
                               WfInstanceMapper instanceMapper,
                               DagParser dagParser,
                               WorkflowExecutor executor) {
        this.definitionMapper = definitionMapper;
        this.instanceMapper = instanceMapper;
        this.dagParser = dagParser;
        this.executor = executor;
    }

    @Override
    public List<WfDefinition> listAll() {
        return definitionMapper.selectAll();
    }

    @Override
    public WfDefinition getById(String id) {
        return definitionMapper.selectById(id);
    }

    @Override
    public WfDefinition create(WfDefinition definition) {
        definition.setId(SnowflakeIdGenerator.nextId());
        definitionMapper.insert(definition);
        return definition;
    }

    @Override
    public WfDefinition update(String id, WfDefinition partial) {
        WfDefinition existing = definitionMapper.selectById(id);
        if (existing == null) throw new IllegalArgumentException("Workflow not found: " + id);
        if (partial.getName() != null) existing.setName(partial.getName());
        if (partial.getDescription() != null) existing.setDescription(partial.getDescription());
        if (partial.getDag() != null) existing.setDag(partial.getDag());
        if (partial.getTimeoutSeconds() != null) existing.setTimeoutSeconds(partial.getTimeoutSeconds());
        if (partial.getRetryPolicy() != null) existing.setRetryPolicy(partial.getRetryPolicy());
        if (partial.getVersion() != null) existing.setVersion(partial.getVersion());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        definitionMapper.update(existing);
        return existing;
    }

    @Override
    public void delete(String id) {
        definitionMapper.softDelete(id);
        log.info("Deleted workflow definition: {}", id);
    }

    @Override
    public List<WfInstance> listInstances(int limit) {
        return instanceMapper.selectRecent(limit);
    }

    @Override
    public WfInstance start(String definitionId, Map<String, Object> input) {
        WfDefinition def = definitionMapper.selectById(definitionId);
        if (def == null) {
            throw new IllegalArgumentException("Workflow definition not found: " + definitionId);
        }
        WfInstance instance = new WfInstance();
        instance.setId(SnowflakeIdGenerator.nextId());
        instance.setDefinitionId(definitionId);
        instance.setDefinitionVersion(def.getVersion());
        instance.setInput(toJson(input));
        instance.setStatus("RUNNING");
        instance.setStartedAt(LocalDateTime.now());
        instanceMapper.insert(instance);

        log.info("Started workflow instance {} for definition {}", instance.getId(), definitionId);

        executor.execute(instance, def, input);

        instanceMapper.update(instance);
        log.info("Workflow instance {} completed with status {}", instance.getId(), instance.getStatus());
        return instance;
    }

    @Override
    public WfInstance getInstance(String instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    public WfInstance signal(String instanceId, Map<String, Object> signalData) {
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Instance not found: " + instanceId);
        }
        if (!"PAUSED".equals(instance.getStatus())) {
            throw new IllegalStateException("Instance is not paused: " + instance.getStatus());
        }

        WfDefinition def = definitionMapper.selectById(instance.getDefinitionId());
        if (def == null) {
            throw new IllegalArgumentException("Definition not found for instance: " + instanceId);
        }

        log.info("Resuming workflow instance {} with signal", instanceId);
        executor.resume(instance, def, signalData);
        instanceMapper.update(instance);
        log.info("Workflow instance {} resumed, status: {}", instanceId, instance.getStatus());
        return instance;
    }

    @Override
    public Map<String, Object> parseDag(String id) {
        WfDefinition def = definitionMapper.selectById(id);
        if (def == null) return Map.of();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("definitionId", id);
        result.put("name", def.getName());
        result.put("nodes", dagParser.parseNodes(def.getDag()));
        result.put("edges", dagParser.parseEdges(def.getDag()));
        result.put("nodeCount", dagParser.countNodes(def.getDag()));
        return result;
    }

    private String toJson(Map<String, Object> input) {
        if (input == null) return "{}";
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(input);
        } catch (Exception e) {
            return "{}";
        }
    }
}
