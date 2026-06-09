package com.datang.aibase.eval.engine;

import java.util.*;

public final class MetricCalculator {

    private MetricCalculator() {}

    public static double mrr(List<Map<String, Object>> results, String expectedKey, List<String> actualKey) {
        if (results == null || results.isEmpty()) return 0.0;
        double sum = 0.0;
        int count = 0;
        for (Map<String, Object> result : results) {
            String expected = (String) result.get(expectedKey);
            if (expected == null) continue;
            count++;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
            if (items == null) continue;
            for (int i = 0; i < items.size(); i++) {
                Object val = items.get(i).get(actualKey.get(0));
                if (expected.equals(val != null ? val.toString() : null)) {
                    sum += 1.0 / (i + 1);
                    break;
                }
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public static double ndcg(List<Map<String, Object>> results, List<String> expectedRelevance, int k) {
        if (results == null || results.isEmpty()) return 0.0;
        double dcg = 0.0;
        double idcg = 0.0;

        for (int i = 0; i < Math.min(results.size(), k); i++) {
            double rel = expectedRelevance.size() > i ? relevance(results.get(i), expectedRelevance.get(i)) : 0.0;
            dcg += (Math.pow(2, rel) - 1) / (Math.log(i + 2) / Math.log(2));
            idcg += 1.0 / (Math.log(i + 2) / Math.log(2));
        }

        return idcg == 0 ? 0.0 : dcg / idcg;
    }

    public static double precisionAtK(List<Map<String, Object>> results, String judgeKey, String expectedValue, int k) {
        if (results == null || results.isEmpty()) return 0.0;
        int relevant = 0;
        int limit = Math.min(results.size(), k);
        for (int i = 0; i < limit; i++) {
            Object val = results.get(i).get(judgeKey);
            if (expectedValue.equals(val != null ? val.toString() : null)) {
                relevant++;
            }
        }
        return (double) relevant / limit;
    }

    public static double recallAtK(List<Map<String, Object>> results, String judgeKey, String expectedValue, int k, int totalRelevant) {
        if (results == null || results.isEmpty() || totalRelevant == 0) return 0.0;
        int relevant = 0;
        int limit = Math.min(results.size(), k);
        for (int i = 0; i < limit; i++) {
            Object val = results.get(i).get(judgeKey);
            if (expectedValue.equals(val != null ? val.toString() : null)) {
                relevant++;
            }
        }
        return (double) relevant / totalRelevant;
    }

    public static double successRate(int passed, int total) {
        return total == 0 ? 0.0 : (double) passed / total;
    }

    public static double toolCallAccuracy(int correctCalls, int totalCalls) {
        return totalCalls == 0 ? 0.0 : (double) correctCalls / totalCalls;
    }

    private static double relevance(Map<String, Object> result, String expected) {
        Object actual = result.get("id");
        if (actual != null && actual.toString().equals(expected)) return 1.0;
        return 0.0;
    }
}
