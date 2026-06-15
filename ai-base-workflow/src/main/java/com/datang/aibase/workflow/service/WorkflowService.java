package com.datang.aibase.workflow.service;

import com.datang.aibase.workflow.entity.WfDefinition;
import com.datang.aibase.workflow.entity.WfInstance;
import com.datang.aibase.workflow.entity.WfTemplate;

import java.util.List;
import java.util.Map;

public interface WorkflowService {

    List<WfDefinition> listAll();

    WfDefinition getById(String id);

    WfDefinition create(WfDefinition definition);

    WfDefinition update(String id, WfDefinition definition);

    void delete(String id);

    List<WfInstance> listInstances(int limit);

    WfInstance start(String definitionId, Map<String, Object> input);

    WfInstance getInstance(String instanceId);

    WfInstance signal(String instanceId, Map<String, Object> signalData);

    Map<String, Object> parseDag(String id);

    // Templates
    List<WfTemplate> listTemplates(String category);
    WfTemplate getTemplate(String id);
    WfTemplate createTemplate(WfTemplate template);
    WfTemplate updateTemplate(String id, WfTemplate template);
    void deleteTemplate(String id);
    WfDefinition instantiateTemplate(String templateId, String name);
}
