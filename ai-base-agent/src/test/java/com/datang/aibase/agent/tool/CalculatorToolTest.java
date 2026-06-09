package com.datang.aibase.agent.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatorToolTest {

    private final CalculatorTool tool = new CalculatorTool();

    @Test
    @DisplayName("getName returns calculator")
    void getName_returnsCalculator() {
        assertThat(tool.getName()).isEqualTo("calculator");
    }

    @Test
    @DisplayName("getDescription is non-blank")
    void getDescription_nonBlank() {
        assertThat(tool.getDescription()).isNotBlank();
    }

    @Test
    @DisplayName("getInputSchema contains expression property")
    void getInputSchema_hasExpression() {
        assertThat(tool.getInputSchema()).contains("expression");
    }

    @Test
    @DisplayName("execute evaluates simple arithmetic")
    void execute_simpleArithmetic() {
        var result = tool.execute(Map.of("expression", "2 + 3 * 4"));

        assertThat(result).containsKey("result");
    }

    @Test
    @DisplayName("execute returns error for blank expression")
    void execute_blankExpression_returnsError() {
        var result = tool.execute(Map.of("expression", "   "));

        assertThat(result).containsKey("error");
    }

    @Test
    @DisplayName("execute returns error for missing expression")
    void execute_missingExpression_returnsError() {
        var result = tool.execute(Map.of());

        assertThat(result).containsKey("error");
    }

    @Test
    @DisplayName("execute returns error for invalid expression")
    void execute_invalidExpression_returnsError() {
        var result = tool.execute(Map.of("expression", "abc + def"));

        assertThat(result).containsKey("error");
    }
}
