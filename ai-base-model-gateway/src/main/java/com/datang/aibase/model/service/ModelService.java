package com.datang.aibase.model.service;

import com.datang.aibase.model.entity.ModelCallLog;
import com.datang.aibase.model.entity.ModelConfig;
import com.datang.aibase.model.entity.ModelRouteRule;

import java.util.List;
import java.util.Map;

public interface ModelService {

    List<ModelConfig> listAll();

    ModelConfig getById(String id);

    ModelConfig create(ModelConfig config);

    ModelConfig update(String id, ModelConfig config);

    void delete(String id);

    List<ModelRouteRule> listRules();

    ModelRouteRule addRule(ModelRouteRule rule);

    List<ModelCallLog> listLogs(int limit);

    Map<String, Object> chat(Map<String, Object> request);

    Map<String, Object> embed(Map<String, Object> request);
}
