package com.datang.aibase.knowledge.service.impl;

import com.datang.aibase.knowledge.mapper.KbChunkMapper;
import com.datang.aibase.knowledge.mapper.KbConfigMapper;
import com.datang.aibase.knowledge.mapper.KbDocumentMapper;
import com.datang.aibase.knowledge.pipeline.EmbeddingService;
import com.datang.aibase.knowledge.pipeline.IngestPipeline;
import com.datang.aibase.knowledge.repository.KnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KbServiceImplTest {

    private KbConfigMapper configMapper;
    private KbDocumentMapper documentMapper;
    private KbChunkMapper chunkMapper;
    private IngestPipeline ingestPipeline;
    private EmbeddingService embeddingService;
    private KnowledgeRepository knowledgeRepository;
    private KbServiceImpl service;

    @BeforeEach
    void setUp() {
        configMapper = mock(KbConfigMapper.class);
        documentMapper = mock(KbDocumentMapper.class);
        chunkMapper = mock(KbChunkMapper.class);
        ingestPipeline = mock(IngestPipeline.class);
        embeddingService = mock(EmbeddingService.class);
        knowledgeRepository = mock(KnowledgeRepository.class);
        service = new KbServiceImpl(configMapper, documentMapper, chunkMapper,
                ingestPipeline, embeddingService, Optional.of(knowledgeRepository));
    }

    @Test
    @DisplayName("listAll returns active KB configs")
    void listAll_returnsActiveConfigs() {
        when(configMapper.selectByStatus("ACTIVE")).thenReturn(List.of());

        var result = service.listAll();

        assertThat(result).isEmpty();
        verify(configMapper).selectByStatus("ACTIVE");
    }

    @Test
    @DisplayName("getById returns null for unknown id")
    void getById_unknown_returnsNull() {
        when(configMapper.selectById("unknown")).thenReturn(null);

        assertThat(service.getById("unknown")).isNull();
    }

    @Test
    @DisplayName("stats returns document and chunk counts")
    void stats_returnsCounts() {
        when(documentMapper.selectByKbId("kb1")).thenReturn(List.of());
        when(chunkMapper.countByKbId("kb1")).thenReturn(42);
        when(knowledgeRepository.count("kb1")).thenReturn(100L);

        Map<String, Object> stats = service.stats("kb1");

        assertThat(stats).containsEntry("kbId", "kb1");
        assertThat(stats).containsEntry("chunkCount", 42);
    }

    @Test
    @DisplayName("scoreKeyword returns 0.8 for exact match")
    void scoreKeyword_exactMatch() {
        double score = invokeScoreKeyword("hello world", "hello");

        assertThat(score).isEqualTo(0.8);
    }

    @Test
    @DisplayName("scoreKeyword returns 0.5 for partial word match")
    void scoreKeyword_partialWordMatch() {
        double score = invokeScoreKeyword("machine learning", "learn");

        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("scoreKeyword returns 0 for no match")
    void scoreKeyword_noMatch() {
        double score = invokeScoreKeyword("hello world", "xyz");

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("search KEYWORD only returns keyword results")
    void search_keywordOnly_returnsKeywordResults() {
        when(knowledgeRepository.search(any(), any(), anyInt())).thenReturn(List.of());
        when(embeddingService.embedSingle(any())).thenReturn(List.of());
        when(chunkMapper.keywordSearch("kb1", "query", 20)).thenReturn(List.of());

        var results = service.search("kb1", "query", 10, 0.0, "KEYWORD");

        assertThat(results).isEmpty();
    }

    private double invokeScoreKeyword(String content, String query) {
        try {
            var method = KbServiceImpl.class.getDeclaredMethod("scoreKeyword", String.class, String.class);
            method.setAccessible(true);
            return (double) method.invoke(service, content, query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
