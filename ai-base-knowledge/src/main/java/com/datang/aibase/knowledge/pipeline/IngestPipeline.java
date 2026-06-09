package com.datang.aibase.knowledge.pipeline;

import com.datang.aibase.common.enums.IngestStatus;
import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.knowledge.entity.KbChunk;
import com.datang.aibase.knowledge.entity.KbConfig;
import com.datang.aibase.knowledge.entity.KbDocument;
import com.datang.aibase.knowledge.mapper.KbChunkMapper;
import com.datang.aibase.knowledge.mapper.KbConfigMapper;
import com.datang.aibase.knowledge.mapper.KbDocumentMapper;
import com.datang.aibase.knowledge.repository.KnowledgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class IngestPipeline {

    private static final Logger log = LoggerFactory.getLogger(IngestPipeline.class);

    private final KbDocumentMapper documentMapper;
    private final KbChunkMapper chunkMapper;
    private final KbConfigMapper configMapper;
    private final EmbeddingService embeddingService;
    private final KnowledgeRepository knowledgeRepository;

    public IngestPipeline(KbDocumentMapper documentMapper, KbChunkMapper chunkMapper,
                          KbConfigMapper configMapper, EmbeddingService embeddingService,
                          Optional<KnowledgeRepository> knowledgeRepository) {
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.configMapper = configMapper;
        this.embeddingService = embeddingService;
        this.knowledgeRepository = knowledgeRepository.orElse(null);
        if (this.knowledgeRepository == null) {
            log.warn("No KnowledgeRepository bean found — vector storage disabled. "
                    + "Set knowledge.repository.type=milvus to enable.");
        }
    }

    public KbDocument ingest(String kbId, String title, String content, String sourceType, String fileType) {
        KbConfig kbConfig = configMapper.selectById(kbId);
        if (kbConfig == null) {
            throw new IllegalArgumentException("Knowledge base not found: " + kbId);
        }

        int chunkSize = kbConfig.getChunkSize() != null ? kbConfig.getChunkSize() : 800;
        int chunkOverlap = kbConfig.getChunkOverlap() != null ? kbConfig.getChunkOverlap() : 100;

        KbDocument doc = new KbDocument();
        doc.setId(SnowflakeIdGenerator.nextId());
        doc.setKbId(kbId);
        doc.setTitle(title);
        doc.setSourceType(com.datang.aibase.common.enums.SourceType.valueOf(sourceType));
        doc.setFileType(fileType);
        doc.setStatus(IngestStatus.PENDING.name());
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        documentMapper.insert(doc);

        try {
            DocumentSplitter splitter = new DocumentSplitter(chunkSize, chunkOverlap);
            List<String> splitTexts = splitter.split(content);

            List<List<Float>> vectors = embeddingService.embed(splitTexts);

            List<KbChunk> chunks = new ArrayList<>();
            List<Map<String, String>> metadataList = new ArrayList<>();
            for (int i = 0; i < splitTexts.size(); i++) {
                KbChunk chunk = new KbChunk();
                chunk.setId(SnowflakeIdGenerator.nextId());
                chunk.setDocId(doc.getId());
                chunk.setKbId(kbId);
                chunk.setChunkIndex(i);
                chunk.setContent(splitTexts.get(i));
                chunk.setTokenCount(splitTexts.get(i).length());
                chunk.setCreatedAt(LocalDateTime.now());
                chunk.setUpdatedAt(LocalDateTime.now());
                chunks.add(chunk);
                metadataList.add(Map.of("doc_id", doc.getId(), "chunk_index", String.valueOf(i)));
            }

            if (!chunks.isEmpty()) {
                chunkMapper.batchInsert(chunks);
                if (knowledgeRepository != null && !vectors.isEmpty()) {
                    List<String> chunkIds = chunks.stream().map(KbChunk::getId).toList();
                    knowledgeRepository.insertVectors(kbId, chunkIds, vectors, metadataList);
                }
            }

            doc.setStatus(IngestStatus.READY.name());
            doc.setChunkCount(chunks.size());
            doc.setIngestedAt(LocalDateTime.now());
            documentMapper.updateStatus(doc);

            log.info("Ingested document {} with {} chunks into KB {}", doc.getId(), chunks.size(), kbId);
        } catch (Exception e) {
            log.error("Ingest failed for doc {}: {}", doc.getId(), e.getMessage(), e);
            doc.setStatus(IngestStatus.FAILED.name());
            documentMapper.updateStatus(doc);
        }

        return doc;
    }
}
