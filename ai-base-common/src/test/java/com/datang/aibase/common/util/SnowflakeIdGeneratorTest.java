package com.datang.aibase.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeIdGeneratorTest {

    @Test
    @DisplayName("nextId returns non-blank numeric string")
    void nextId_returnsNonBlankNumeric() {
        String id = SnowflakeIdGenerator.nextId();

        assertThat(id).isNotBlank();
        assertThat(Long.parseLong(id)).isPositive();
    }

    @Test
    @DisplayName("nextId returns unique values under concurrent load")
    void nextId_unique() {
        int count = 10_000;
        Set<String> ids = ConcurrentHashMap.newKeySet();

        IntStream.range(0, count).parallel().forEach(i -> ids.add(SnowflakeIdGenerator.nextId()));

        assertThat(ids).hasSize(count);
    }

    @Test
    @DisplayName("nextId returns monotonically increasing values")
    void nextId_monotonicallyIncreasing() {
        long prev = Long.parseLong(SnowflakeIdGenerator.nextId());
        for (int i = 0; i < 1000; i++) {
            long curr = Long.parseLong(SnowflakeIdGenerator.nextId());
            assertThat(curr).isGreaterThan(prev);
            prev = curr;
        }
    }
}
