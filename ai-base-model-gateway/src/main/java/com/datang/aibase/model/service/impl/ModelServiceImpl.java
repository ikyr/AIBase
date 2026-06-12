package com.datang.aibase.model.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.model.entity.ModelCallLog;
import com.datang.aibase.model.entity.ModelConfig;
import com.datang.aibase.model.entity.ModelRouteRule;
import com.datang.aibase.model.mapper.ModelCallLogMapper;
import com.datang.aibase.model.mapper.ModelConfigMapper;
import com.datang.aibase.model.mapper.ModelRouteRuleMapper;
import com.datang.aibase.model.provider.ModelProvider;
import com.datang.aibase.model.router.ModelRouter;
import com.datang.aibase.model.service.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger log = LoggerFactory.getLogger(ModelServiceImpl.class);

    private final ModelConfigMapper configMapper;
    private final ModelRouteRuleMapper ruleMapper;
    private final ModelCallLogMapper callLogMapper;
    private final ModelRouter router;
    private final Map<String, ModelProvider> providers;

    public ModelServiceImpl(ModelConfigMapper configMapper,
                            ModelRouteRuleMapper ruleMapper,
                            ModelCallLogMapper callLogMapper,
                            ModelRouter router,
                            List<ModelProvider> providerList) {
        this.configMapper = configMapper;
        this.ruleMapper = ruleMapper;
        this.callLogMapper = callLogMapper;
        this.router = router;
        this.providers = providerList.stream()
                .collect(Collectors.toMap(ModelProvider::getProviderName, Function.identity()));
    }

    @Override
    public List<ModelConfig> listAll() {
        return configMapper.selectAll();
    }

    @Override
    public ModelConfig getById(String id) {
        return configMapper.selectById(id);
    }

    @Override
    public ModelConfig create(ModelConfig config) {
        config.setId(SnowflakeIdGenerator.nextId());
        configMapper.insert(config);
        return config;
    }

    @Override
    public ModelConfig update(String id, ModelConfig config) {
        config.setId(id);
        config.setUpdatedAt(java.time.LocalDateTime.now());
        configMapper.update(config);
        return configMapper.selectById(id);
    }

    @Override
    public void delete(String id) {
        configMapper.softDelete(id);
        log.info("Deleted model: {}", id);
    }

    @Override
    public List<ModelRouteRule> listRules() {
        return ruleMapper.selectActive();
    }

    @Override
    public ModelRouteRule addRule(ModelRouteRule rule) {
        rule.setId(SnowflakeIdGenerator.nextId());
        ruleMapper.insert(rule);
        return rule;
    }

    @Override
    public List<ModelCallLog> listLogs(int limit) {
        return callLogMapper.selectRecent(limit);
    }

    @Override
    public Map<String, Object> chat(Map<String, Object> request) {
        ModelConfig model = router.route(request);
        if (model == null) {
            return Map.of("error", "No available model found");
        }
        ModelProvider provider = providers.get(model.getProvider().toUpperCase());
        if (provider == null) {
            return Map.of("error", "No provider found for: " + model.getProvider());
        }
        log.info("Routing chat to {} via {}", model.getName(), provider.getProviderName());
        return provider.chat(model.getEndpoint(), model.getApiKeyRef(), model.getName(), request);
    }

    @Override
    public Map<String, Object> embed(Map<String, Object> request) {
        ModelConfig model = router.route(request);
        if (model == null) {
            return Map.of("error", "No available model found for embedding");
        }
        ModelProvider provider = providers.get(model.getProvider().toUpperCase());
        if (provider == null) {
            return Map.of("error", "No provider found for: " + model.getProvider());
        }
        String text = (String) request.getOrDefault("text", "");
        log.info("Routing embedding to {} via {}", model.getName(), provider.getProviderName());
        return provider.embed(model.getEndpoint(), model.getApiKeyRef(), model.getName(), text);
    }
}
