package com.datang.aibase.model.router;

import com.datang.aibase.model.entity.ModelConfig;
import com.datang.aibase.model.entity.ModelRouteRule;
import com.datang.aibase.model.mapper.ModelConfigMapper;
import com.datang.aibase.model.mapper.ModelRouteRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final ModelConfigMapper configMapper;
    private final ModelRouteRuleMapper ruleMapper;

    public ModelRouter(ModelConfigMapper configMapper, ModelRouteRuleMapper ruleMapper) {
        this.configMapper = configMapper;
        this.ruleMapper = ruleMapper;
    }

    public ModelConfig route(Map<String, Object> context) {
        List<ModelRouteRule> rules = ruleMapper.selectActive();
        List<ModelConfig> models = configMapper.selectActive();

        for (ModelRouteRule rule : rules) {
            if (matchRule(rule, context)) {
                return models.stream()
                        .filter(m -> m.getId().equals(rule.getModelId()))
                        .findFirst()
                        .orElse(getDefaultModel(models));
            }
        }
        return getDefaultModel(models);
    }

    public ModelConfig getDefaultModel(List<ModelConfig> models) {
        return models.stream()
                .filter(m -> m.getPriority() != null && m.getPriority() == 0)
                .findFirst()
                .orElse(models.isEmpty() ? null : models.get(0));
    }

    private boolean matchRule(ModelRouteRule rule, Map<String, Object> context) {
        String expr = rule.getMatchExpression();
        if (expr == null || expr.isBlank() || expr.equals("*")) return true;

        // 1. Regex match: "key =~ /pattern/"
        if (expr.contains("=~")) {
            String[] parts = expr.split("=~", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String pattern = parts[1].trim().replaceAll("^/|/$", "");
                Object actual = context.get(key);
                return actual != null && actual.toString().matches(pattern);
            }
        }

        // 2. Not-equal: "key != value"
        if (expr.contains("!=")) {
            String[] parts = expr.split("!=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expected = parts[1].trim().replace("\"", "");
                Object actual = context.get(key);
                return actual != null && !expected.equals(actual.toString());
            }
        }

        // 3. In-list: "key in [v1, v2, v3]"
        if (expr.contains(" in ")) {
            String[] parts = expr.split(" in ", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String listStr = parts[1].trim().replaceAll("[\\[\\]]", "");
                String[] values = listStr.split("\\s*,\\s*");
                Object actual = context.get(key);
                if (actual != null) {
                    for (String v : values) {
                        if (v.trim().replace("\"", "").equals(actual.toString())) return true;
                    }
                }
                return false;
            }
        }

        // 4. Default: key == value
        if (expr.contains("==")) {
            String[] parts = expr.split("==", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expected = parts[1].trim().replace("\"", "");
                Object actual = context.get(key);
                return expected.equals(actual != null ? actual.toString() : null);
            }
        }

        log.warn("Unrecognized match expression: {}", expr);
        return false;
    }
}
