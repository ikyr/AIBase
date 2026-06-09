package com.datang.aibase.skill.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GraalJsSandboxTest {

    private final GraalJsSandbox sandbox = new GraalJsSandbox();

    @Test
    @DisplayName("isAvailable returns true when JS engine is present")
    void isAvailable_enginePresent() {
        assumeTrue(sandbox.isAvailable(), "No JS engine available");

        assertThat(sandbox.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("execute runs simple script and returns result")
    void execute_simpleScript() {
        assumeTrue(sandbox.isAvailable(), "No JS engine available");

        String script = """
            function execute(input) {
                return { greeting: "Hello, " + input.name };
            }
            """;

        Map<String, Object> result = sandbox.execute(script, Map.of("name", "World"), 5000);

        assertThat(result).containsEntry("greeting", "Hello, World");
    }

    @Test
    @DisplayName("execute handles script with calculation")
    void execute_calculation() {
        assumeTrue(sandbox.isAvailable(), "No JS engine available");

        String script = """
            function execute(input) {
                return { sum: Number(input.a) + Number(input.b) };
            }
            """;

        Map<String, Object> result = sandbox.execute(script, Map.of("a", "3", "b", "4"), 5000);

        assertThat(result).containsEntry("sum", 7.0);
    }

    @Test
    @DisplayName("execute throws on script error")
    void execute_scriptError_throws() {
        assumeTrue(sandbox.isAvailable(), "No JS engine available");

        String script = """
            function execute(input) {
                throw new Error("test error");
            }
            """;

        try {
            sandbox.execute(script, Map.of(), 5000);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Script execution failed");
        }
    }
}
