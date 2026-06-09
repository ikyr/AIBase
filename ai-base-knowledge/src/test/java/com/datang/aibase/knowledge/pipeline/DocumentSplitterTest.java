package com.datang.aibase.knowledge.pipeline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSplitterTest {

    @Test
    @DisplayName("split returns single chunk for short content")
    void split_shortContent_singleChunk() {
        var splitter = new DocumentSplitter(800, 100);

        List<String> result = splitter.split("Hello world");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("Hello world");
    }

    @Test
    @DisplayName("split returns multiple chunks for long content")
    void split_longContent_multipleChunks() {
        var splitter = new DocumentSplitter(10, 3);
        String content = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        List<String> result = splitter.split(content);

        assertThat(result.size()).isGreaterThan(1);
        assertThat(String.join("", result)).contains("ABCDEFGHIJ");
    }

    @Test
    @DisplayName("split handles empty content")
    void split_emptyContent_returnsEmpty() {
        var splitter = new DocumentSplitter(800, 100);

        List<String> result = splitter.split("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("split handles null content")
    void split_nullContent_returnsEmpty() {
        var splitter = new DocumentSplitter(800, 100);

        List<String> result = splitter.split(null);

        assertThat(result).isEmpty();
    }
}
