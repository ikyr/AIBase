package com.datang.aibase.knowledge.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "knowledge.repository.type", havingValue = "ragflow")
public class RagFlowKnowledgeRepository implements KnowledgeRepository {

    private static final Logger log = LoggerFactory.getLogger(RagFlowKnowledgeRepository.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RestClient restClient;
    private final RagFlowProperties properties;

    public RagFlowKnowledgeRepository(RagFlowProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .build();
    }

    @Override
    public void createCollection(String kbId, int dimension) {
        try {
            Map<String, Object> body = Map.of(
                    "name", "kb_" + kbId,
                    "description", "Auto-created by AIBase",
                    "embedding_model", properties.getEmbeddingModel() != null
                            ? properties.getEmbeddingModel() : "text-embedding-v3"
            );

            String response = restClient.post()
                    .uri(properties.getEndpoint() + "/api/v1/datasets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("Created RAGFlow dataset for kb {}: {}", kbId, response);
        } catch (Exception e) {
            log.error("Failed to create RAGFlow dataset for kb {}: {}", kbId, e.getMessage());
            throw new RuntimeException("RAGFlow dataset creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropCollection(String kbId) {
        try {
            String datasetId = findDatasetId("kb_" + kbId);
            if (datasetId != null) {
                restClient.delete()
                        .uri(properties.getEndpoint() + "/api/v1/datasets/" + datasetId)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Dropped RAGFlow dataset for kb {}", kbId);
            }
        } catch (Exception e) {
            log.error("Failed to drop RAGFlow dataset for kb {}: {}", kbId, e.getMessage());
        }
    }

    @Override
    public void insertVectors(String kbId, List<String> ids, List<List<Float>> vectors, List<Map<String, String>> metadataList) {
        String datasetId = findDatasetId("kb_" + kbId);
        if (datasetId == null) {
            throw new RuntimeException("RAGFlow dataset not found for kb " + kbId);
        }

        for (int i = 0; i < ids.size(); i++) {
            try {
                Map<String, String> meta = metadataList != null && i < metadataList.size()
                        ? metadataList.get(i) : Map.of();
                String content = meta.getOrDefault("content", "");

                Map<String, Object> body = new HashMap<>();
                body.put("name", ids.get(i));
                body.put("content", content);
                body.put("metadata", meta);

                restClient.post()
                        .uri(properties.getEndpoint() + "/api/v1/datasets/" + datasetId + "/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                if ((i + 1) % 50 == 0) {
                    log.info("Uploaded {}/{} documents to RAGFlow dataset {}", i + 1, ids.size(), datasetId);
                }
            } catch (Exception e) {
                log.warn("Failed to upload document {} to RAGFlow: {}", ids.get(i), e.getMessage());
            }
        }
        log.info("Finished uploading {} documents to RAGFlow dataset {}", ids.size(), datasetId);
    }

    @Override
    public List<SearchHit> search(String kbId, List<Float> queryVector, int topK) {
        String datasetId = findDatasetId("kb_" + kbId);
        if (datasetId == null) {
            log.warn("RAGFlow dataset not found for kb {}", kbId);
            return List.of();
        }

        try {
            Map<String, Object> body = Map.of(
                    "question", "",
                    "top_k", topK,
                    "embeddings", queryVector
            );

            String response = restClient.post()
                    .uri(properties.getEndpoint() + "/api/v1/datasets/" + datasetId + "/chunks/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseSearchResults(response);
        } catch (Exception e) {
            log.error("RAGFlow search failed for kb {}: {}", kbId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteByDocId(String kbId, String docId) {
        String datasetId = findDatasetId("kb_" + kbId);
        if (datasetId == null) return;

        try {
            restClient.delete()
                    .uri(properties.getEndpoint() + "/api/v1/datasets/" + datasetId + "/documents/" + docId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete document {} from RAGFlow: {}", docId, e.getMessage());
        }
    }

    @Override
    public long count(String kbId) {
        String datasetId = findDatasetId("kb_" + kbId);
        if (datasetId == null) return 0;

        try {
            String response = restClient.get()
                    .uri(properties.getEndpoint() + "/api/v1/datasets/" + datasetId)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(response);
            JsonNode data = root.get("data");
            if (data != null && data.has("document_count")) {
                return data.get("document_count").asLong();
            }
        } catch (Exception e) {
            log.warn("Failed to get count from RAGFlow: {}", e.getMessage());
        }
        return 0;
    }

    private String findDatasetId(String datasetName) {
        try {
            String response = restClient.get()
                    .uri(properties.getEndpoint() + "/api/v1/datasets?name=" + datasetName)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(response);
            JsonNode data = root.get("data");
            if (data != null && data.isArray() && !data.isEmpty()) {
                return data.get(0).get("id").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to find RAGFlow dataset {}: {}", datasetName, e.getMessage());
        }
        return null;
    }

    private List<SearchHit> parseSearchResults(String response) {
        List<SearchHit> hits = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode chunks = root.at("/data/chunks");
            if (chunks.isArray()) {
                for (JsonNode chunk : chunks) {
                    String id = chunk.has("id") ? chunk.get("id").asText() : "";
                    float score = chunk.has("similarity") ? (float) chunk.get("similarity").asDouble() : 0f;
                    Map<String, String> metadata = new HashMap<>();
                    if (chunk.has("content")) {
                        metadata.put("content", chunk.get("content").asText());
                    }
                    if (chunk.has("document_name")) {
                        metadata.put("title", chunk.get("document_name").asText());
                    }
                    hits.add(new SearchHit(id, score, metadata));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse RAGFlow search results: {}", e.getMessage());
        }
        return hits;
    }
}
