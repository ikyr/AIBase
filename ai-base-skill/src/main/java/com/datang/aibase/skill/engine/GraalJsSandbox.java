package com.datang.aibase.skill.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GraalJsSandbox {

    private static final Logger log = LoggerFactory.getLogger(GraalJsSandbox.class);
    private static final int DEFAULT_TIMEOUT_MS = 10_000;

    private final ScriptEngine engine;

    public GraalJsSandbox() {
        ScriptEngine found = null;
        for (String name : List.of("graal.js", "JavaScript", "nashorn", "js")) {
            found = new ScriptEngineManager().getEngineByName(name);
            if (found != null) break;
        }
        this.engine = found;
        if (engine == null) {
            log.warn("No JavaScript engine found — Function skill sandbox will use LLM fallback only");
        } else {
            log.info("JavaScript sandbox initialized: {}", engine.getFactory().getEngineName());
        }
    }

    public boolean isAvailable() {
        return engine != null;
    }

    public Map<String, Object> execute(String script, Map<String, Object> input, int timeoutMs) {
        if (!isAvailable()) {
            throw new UnsupportedOperationException("GraalJS engine not available");
        }

        int effectiveTimeout = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "graaljs-sandbox");
            t.setDaemon(true);
            return t;
        });

        Future<Map<String, Object>> future = executor.submit(() -> {
            try {
                engine.eval("var input = JSON.parse('" + escapeJson(input) + "');");
                engine.eval(script);
                engine.eval("var __result__ = execute(input);");

                Invocable invocable = (Invocable) engine;
                Object result = invocable.invokeFunction("execute", input.toString());

                if (result instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mapResult = (Map<String, Object>) result;
                    return mapResult;
                }
                return Map.of("result", result != null ? result.toString() : "null");
            } catch (Exception e) {
                throw new RuntimeException("Script execution failed: " + e.getMessage(), e);
            }
        });

        try {
            return future.get(effectiveTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Script execution timed out after " + effectiveTimeout + "ms");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            throw new RuntimeException(cause != null ? cause.getMessage() : e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    private String escapeJson(Map<String, Object> input) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey())).append("\":\"")
                    .append(escapeString(String.valueOf(entry.getValue()))).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
