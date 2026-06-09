package com.datang.aibase.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import java.util.Map;

@Component
public class CalculatorTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    @Override
    public String getName() { return "calculator"; }

    @Override
    public String getDescription() { return "Evaluate a mathematical expression. Input: expression (string, e.g. '2+3*4')."; }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\"}},\"required\":[\"expression\"]}";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        String expression = (String) input.get("expression");
        if (expression == null || expression.isBlank()) {
            return Map.of("error", "No expression provided");
        }
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
            if (engine == null) {
                double result = simpleEval(expression);
                return Map.of("expression", expression, "result", result);
            }
            Object result = engine.eval(expression);
            return Map.of("expression", expression, "result", result);
        } catch (Exception e) {
            log.warn("Calculator evaluation failed for '{}': {}", expression, e.getMessage());
            return Map.of("error", "Failed to evaluate: " + e.getMessage());
        }
    }

    private double simpleEval(String expr) {
        String sanitized = expr.replaceAll("[^0-9+\\-*/().]", "");
        String[] parts = sanitized.split("(?=[+\\-*/])|(?<=[+\\-*/])");
        if (parts.length == 0) return 0;
        try {
            double result = Double.parseDouble(parts[0].trim());
            for (int i = 1; i < parts.length - 1; i += 2) {
                String op = parts[i].trim();
                double next = Double.parseDouble(parts[i + 1].trim());
                result = switch (op) {
                    case "+" -> result + next;
                    case "-" -> result - next;
                    case "*" -> result * next;
                    case "/" -> result / next;
                    default -> result;
                };
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot evaluate: " + expr);
        }
    }
}
