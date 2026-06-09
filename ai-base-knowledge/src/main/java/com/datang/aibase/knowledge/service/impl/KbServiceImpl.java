package com.datang.aibase.knowledge.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.knowledge.entity.KbChunk;
import com.datang.aibase.knowledge.entity.KbConfig;
import com.datang.aibase.knowledge.mapper.KbChunkMapper;
import com.datang.aibase.knowledge.mapper.KbConfigMapper;
import com.datang.aibase.knowledge.mapper.KbDocumentMapper;
import com.datang.aibase.knowledge.model.KbInfo;
import com.datang.aibase.knowledge.pipeline.EmbeddingService;
import com.datang.aibase.knowledge.pipeline.IngestPipeline;
import com.datang.aibase.knowledge.repository.KnowledgeRepository;
import com.datang.aibase.knowledge.service.KbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KbServiceImpl implements KbService {

    private static final Logger log = LoggerFactory.getLogger(KbServiceImpl.class);

    private final KbConfigMapper kbConfigMapper;
    private final KbDocumentMapper documentMapper;
    private final KbChunkMapper chunkMapper;
    private final IngestPipeline ingestPipeline;
    private final EmbeddingService embeddingService;
    private final KnowledgeRepository knowledgeRepository;

    public KbServiceImpl(KbConfigMapper kbConfigMapper, KbDocumentMapper documentMapper,
                         KbChunkMapper chunkMapper, IngestPipeline ingestPipeline,
                         EmbeddingService embeddingService,
                         Optional<KnowledgeRepository> knowledgeRepository) {
        this.kbConfigMapper = kbConfigMapper;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.ingestPipeline = ingestPipeline;
        this.embeddingService = embeddingService;
        this.knowledgeRepository = knowledgeRepository.orElse(null);
        if (this.knowledgeRepository == null) {
            log.warn("No KnowledgeRepository bean found — vector search disabled. "
                    + "Set knowledge.repository.type=milvus to enable.");
        }
    }

    @Override
    public List<KbInfo> listAll() {
        List<KbConfig> configs = kbConfigMapper.selectByStatus("ACTIVE");
        return configs.stream().map(this::toKbInfo).collect(Collectors.toList());
    }

    @Override
    public KbInfo getById(String id) {
        KbConfig config = kbConfigMapper.selectById(id);
        if (config == null) return null;
        KbInfo info = toKbInfo(config);
        int docCount = documentMapper.selectByKbId(id).size();
        int chunkCount = chunkMapper.countByKbId(id);
        info.setDocumentCount(docCount);
        info.setChunkCount(chunkCount);
        return info;
    }

    @Override
    public List<com.datang.aibase.knowledge.model.KbDocumentDto> getDocuments(String kbId) {
        List<com.datang.aibase.knowledge.entity.KbDocument> docs = documentMapper.selectByKbId(kbId);
        return docs.stream().map(d -> {
            com.datang.aibase.knowledge.model.KbDocumentDto md = new com.datang.aibase.knowledge.model.KbDocumentDto();
            md.setId(d.getId());
            md.setKbId(d.getKbId());
            md.setFileName(d.getTitle());
            md.setFileType(d.getFileType());
            md.setChunkCount(d.getChunkCount() != null ? d.getChunkCount() : 0);
            md.setUploadStatus(d.getStatus());
            md.setUploadedAt(d.getIngestedAt() != null ? d.getIngestedAt().toString() : "");
            return md;
        }).collect(Collectors.toList());
    }

    @Override
    public KbInfo create(String name, String description) {
        return create(name, description, "PUBLIC", 800, 100);
    }

    @Override
    public KbInfo create(String name, String description, String kbType, int chunkSize, int chunkOverlap) {
        KbConfig config = new KbConfig();
        config.setId(SnowflakeIdGenerator.nextId());
        config.setName(name);
        config.setDescription(description);
        config.setKbType(kbType);
        config.setChunkSize(chunkSize);
        config.setChunkOverlap(chunkOverlap);
        config.setEmbeddingModel("text-embedding-v3");
        config.setStatus("ACTIVE");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        kbConfigMapper.insert(config);

        if (knowledgeRepository != null) {
            knowledgeRepository.createCollection(config.getId(), 1536);
        }
        log.info("Created KB: id={}, name={}", config.getId(), name);
        return toKbInfo(config);
    }

    @Override
    public com.datang.aibase.knowledge.entity.KbDocument ingest(String kbId, String title, String content, String sourceType, String fileType) {
        return ingestPipeline.ingest(kbId, title, content, sourceType, fileType);
    }

    @Override
    public void deleteDocument(String docId) {
        com.datang.aibase.knowledge.entity.KbDocument doc = documentMapper.selectById(docId);
        if (doc != null) {
            if (knowledgeRepository != null) {
                knowledgeRepository.deleteByDocId(doc.getKbId(), docId);
            }
            chunkMapper.deleteByDocId(docId);
        }
        documentMapper.softDelete(docId);
    }

    @Override
    public List<Map<String, Object>> search(String kbId, String query, int topK) {
        return search(kbId, query, topK, 0.0, "HYBRID");
    }

    @Override
    public List<Map<String, Object>> search(String kbId, String query, int topK, double threshold, String searchType) {
        List<KnowledgeRepository.SearchHit> vectorHits = List.of();
        List<KbChunk> keywordChunks = List.of();

        if ("VECTOR".equalsIgnoreCase(searchType) || "HYBRID".equalsIgnoreCase(searchType)) {
            if (knowledgeRepository != null) {
                List<Float> queryVector = embeddingService.embedSingle(query);
                if (!queryVector.isEmpty()) {
                    vectorHits = knowledgeRepository.search(kbId, queryVector, topK * 2);
                }
            } else {
                log.debug("KnowledgeRepository not available, vector search skipped");
            }
        }

        if ("KEYWORD".equalsIgnoreCase(searchType) || "HYBRID".equalsIgnoreCase(searchType)) {
            keywordChunks = chunkMapper.keywordSearch(kbId, query, topK * 2);
        }

        if ("KEYWORD".equalsIgnoreCase(searchType)) {
            return keywordChunks.stream()
                    .filter(c -> threshold <= 0 || scoreKeyword(c.getContent(), query) >= threshold)
                    .limit(topK)
                    .map(c -> chunkToMap(c, scoreKeyword(c.getContent(), query)))
                    .collect(Collectors.toList());
        }

        if ("VECTOR".equalsIgnoreCase(searchType)) {
            return enrichWithChunks(vectorHits, threshold);
        }

        return mergeResults(vectorHits, keywordChunks, topK, threshold);
    }

    private List<Map<String, Object>> enrichWithChunks(List<KnowledgeRepository.SearchHit> hits, double threshold) {
        if (hits.isEmpty()) return List.of();
        List<String> chunkIds = hits.stream().map(KnowledgeRepository.SearchHit::id).toList();
        Map<String, KbChunk> chunkMap = chunkMapper.selectByIds(chunkIds).stream()
                .collect(Collectors.toMap(KbChunk::getId, c -> c, (a, b) -> a));
        return hits.stream()
                .filter(h -> threshold <= 0 || h.score() >= threshold)
                .map(h -> {
                    KbChunk chunk = chunkMap.get(h.id());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("chunkId", h.id());
                    m.put("score", h.score());
                    m.put("docId", h.metadata().get("doc_id"));
                    m.put("content", chunk != null ? chunk.getContent() : "");
                    m.put("chunkIndex", h.metadata().get("chunk_index"));
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> mergeResults(List<KnowledgeRepository.SearchHit> vectorHits,
                                                    List<KbChunk> keywordChunks, int topK, double threshold) {
        Map<String, Double> scores = new LinkedHashMap<>();
        Map<String, KbChunk> chunkData = new LinkedHashMap<>();
        Map<String, String> docIds = new LinkedHashMap<>();

        int rank = 1;
        for (KnowledgeRepository.SearchHit hit : vectorHits) {
            if (threshold <= 0 || hit.score() >= threshold) {
                double rrf = 1.0 / (60 + rank);
                scores.merge(hit.id(), rrf, Double::sum);
                rank++;
            }
        }
        rank = 1;
        for (KbChunk chunk : keywordChunks) {
            double kwScore = scoreKeyword(chunk.getContent(), "");
            if (threshold <= 0 || kwScore >= threshold) {
                double rrf = 1.0 / (60 + rank);
                scores.merge(chunk.getId(), rrf, Double::sum);
                rank++;
            }
            chunkData.putIfAbsent(chunk.getId(), chunk);
        }

        List<String> missingIds = scores.keySet().stream()
                .filter(id -> !chunkData.containsKey(id))
                .toList();
        if (!missingIds.isEmpty()) {
            Map<String, KbChunk> loaded = chunkMapper.selectByIds(missingIds).stream()
                    .collect(Collectors.toMap(KbChunk::getId, c -> c, (a, b) -> a));
            chunkData.putAll(loaded);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> {
                    KbChunk chunk = chunkData.get(e.getKey());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("chunkId", e.getKey());
                    m.put("score", e.getValue());
                    m.put("docId", docIds.getOrDefault(e.getKey(), ""));
                    m.put("content", chunk != null ? chunk.getContent() : "");
                    return m;
                }).collect(Collectors.toList());
    }

    private Map<String, Object> chunkToMap(KbChunk chunk, double score) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("chunkId", chunk.getId());
        m.put("score", score);
        m.put("docId", chunk.getDocId());
        m.put("content", chunk.getContent());
        m.put("chunkIndex", String.valueOf(chunk.getChunkIndex()));
        return m;
    }

    private double scoreKeyword(String content, String query) {
        if (query == null || query.isBlank() || content == null) return 0;
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();
        if (lowerContent.contains(lowerQuery)) return 0.8;
        for (String word : lowerQuery.split("\\s+")) {
            if (word.length() > 1 && lowerContent.contains(word)) return 0.5;
        }
        return 0;
    }

    @Override
    public Map<String, Object> stats(String kbId) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("kbId", kbId);
        s.put("documentCount", documentMapper.selectByKbId(kbId).size());
        s.put("chunkCount", chunkMapper.countByKbId(kbId));
        if (knowledgeRepository != null) {
            s.put("vectorCount", knowledgeRepository.count(kbId));
        }
        return s;
    }

    private KbInfo toKbInfo(KbConfig c) {
        KbInfo info = new KbInfo();
        info.setId(c.getId());
        info.setName(c.getName());
        info.setDescription(c.getDescription());
        info.setKbType(c.getKbType());
        info.setChunkSize(c.getChunkSize() != null ? c.getChunkSize() : 800);
        info.setChunkOverlap(c.getChunkOverlap() != null ? c.getChunkOverlap() : 100);
        info.setStatus(c.getStatus());
        info.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
        return info;
    }
}
