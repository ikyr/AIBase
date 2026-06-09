package com.datang.aibase.eval.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class MetricCalculatorTest {

    @Test
    @DisplayName("successRate computes correct ratio")
    void successRate_computesRatio() {
        assertThat(MetricCalculator.successRate(8, 10)).isCloseTo(0.8, offset(0.001));
        assertThat(MetricCalculator.successRate(0, 10)).isCloseTo(0.0, offset(0.001));
        assertThat(MetricCalculator.successRate(10, 10)).isCloseTo(1.0, offset(0.001));
    }

    @Test
    @DisplayName("successRate returns 0 when total is 0")
    void successRate_zeroTotal_returnsZero() {
        assertThat(MetricCalculator.successRate(0, 0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("toolCallAccuracy computes correct ratio")
    void toolCallAccuracy_computesRatio() {
        assertThat(MetricCalculator.toolCallAccuracy(7, 10)).isCloseTo(0.7, offset(0.001));
        assertThat(MetricCalculator.toolCallAccuracy(0, 0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("mrr returns 0 for empty results")
    void mrr_emptyResults_returnsZero() {
        double result = MetricCalculator.mrr(List.of(), "expected", List.of("id"));
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("precisionAtK computes correct ratio")
    void precisionAtK_computesRatio() {
        List<Map<String, Object>> results = List.of(
            Map.of("id", "a"),
            Map.of("id", "b"),
            Map.of("id", "c")
        );

        double p = MetricCalculator.precisionAtK(results, "id", "a", 3);

        assertThat(p).isCloseTo(1.0 / 3.0, offset(0.001));
    }

    @Test
    @DisplayName("precisionAtK returns 0 for empty results")
    void precisionAtK_empty_returnsZero() {
        assertThat(MetricCalculator.precisionAtK(List.of(), "id", "x", 5)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("recallAtK returns 0 when totalRelevant is 0")
    void recallAtK_zeroRelevant_returnsZero() {
        List<Map<String, Object>> results = List.of(Map.of("id", "a"));

        assertThat(MetricCalculator.recallAtK(results, "id", "a", 5, 0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ndcg returns 0 for empty results")
    void ndcg_empty_returnsZero() {
        assertThat(MetricCalculator.ndcg(List.of(), List.of("a"), 5)).isEqualTo(0.0);
    }
}
