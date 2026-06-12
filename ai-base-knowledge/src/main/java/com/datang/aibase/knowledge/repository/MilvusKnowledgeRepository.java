package com.datang.aibase.knowledge.repository;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnBean(MilvusServiceClient.class)
public class MilvusKnowledgeRepository implements KnowledgeRepository {

    private static final Logger log = LoggerFactory.getLogger(MilvusKnowledgeRepository.class);
    private static final String VECTOR_FIELD = "embedding";
    private static final String ID_FIELD = "chunk_id";

    private final MilvusServiceClient client;

    public MilvusKnowledgeRepository(MilvusServiceClient client) {
        this.client = client;
    }

    static String collectionName(String kbId) {
        return "kb_" + kbId.replace("-", "_");
    }

    @Override
    public void createCollection(String kbId, int dimension) {
        String name = collectionName(kbId);
        try {
            R<Boolean> hasColl = client.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName(name).build());
            if (hasColl.getData() != null && hasColl.getData()) {
                log.info("Collection {} already exists", name);
                return;
            }

            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(name)
                    .withDescription("KB: " + kbId)
                    .addFieldType(io.milvus.param.collection.FieldType.newBuilder()
                            .withName(ID_FIELD).withDataType(DataType.VarChar)
                            .withPrimaryKey(true).withMaxLength(32).build())
                    .addFieldType(io.milvus.param.collection.FieldType.newBuilder()
                            .withName("doc_id").withDataType(DataType.VarChar)
                            .withMaxLength(32).build())
                    .addFieldType(io.milvus.param.collection.FieldType.newBuilder()
                            .withName("chunk_index").withDataType(DataType.Int32).build())
                    .addFieldType(io.milvus.param.collection.FieldType.newBuilder()
                            .withName(VECTOR_FIELD).withDataType(DataType.FloatVector)
                            .withDimension(dimension).build())
                    .build();

            R<RpcStatus> result = client.createCollection(createParam);
            if (result.getStatus() != 0) {
                log.error("Failed to create collection {}: {}", name, result.getMessage());
                return;
            }

            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(name)
                    .withFieldName(VECTOR_FIELD)
                    .withIndexType(io.milvus.param.IndexType.IVF_FLAT)
                    .withMetricType(MetricType.COSINE)
                    .withExtraParam("{\"nlist\":128}")
                    .build();
            R<RpcStatus> idxResult = client.createIndex(indexParam);
            if (idxResult.getStatus() != 0) {
                log.error("Failed to create index for {}: {}", name, idxResult.getMessage());
            }

            client.loadCollection(io.milvus.param.collection.LoadCollectionParam.newBuilder()
                    .withCollectionName(name).build());
            log.info("Created collection {} with dimension {}", name, dimension);
        } catch (Exception e) {
            log.error("Error creating collection {}: {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void dropCollection(String kbId) {
        String name = collectionName(kbId);
        try {
            client.dropCollection(DropCollectionParam.newBuilder()
                    .withCollectionName(name).build());
            log.info("Dropped collection {}", name);
        } catch (Exception e) {
            log.error("Error dropping collection {}: {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void insertVectors(String kbId, List<String> ids, List<List<Float>> vectors, List<Map<String, String>> metadata) {
        String name = collectionName(kbId);
        try {
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field(ID_FIELD, ids));
            fields.add(new InsertParam.Field(VECTOR_FIELD, vectors));

            List<String> docIds = new ArrayList<>();
            List<Integer> chunkIndices = new ArrayList<>();
            for (Map<String, String> meta : metadata) {
                docIds.add(meta.getOrDefault("doc_id", ""));
                chunkIndices.add(Integer.parseInt(meta.getOrDefault("chunk_index", "0")));
            }
            fields.add(new InsertParam.Field("doc_id", docIds));
            fields.add(new InsertParam.Field("chunk_index", chunkIndices));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(name)
                    .withFields(fields)
                    .build();

            R<MutationResult> result = client.insert(insertParam);
            if (result.getStatus() != 0) {
                log.error("Failed to insert vectors into {}: {}", name, result.getMessage());
            } else {
                client.flush(io.milvus.param.collection.FlushParam.newBuilder()
                        .addCollectionName(name).build());
                log.info("Inserted {} vectors into {}", ids.size(), name);
            }
        } catch (Exception e) {
            log.error("Error inserting vectors into {}: {}", name, e.getMessage(), e);
        }
    }

    @Override
    public List<SearchHit> search(String kbId, List<Float> queryVector, int topK) {
        String name = collectionName(kbId);
        try {
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(name)
                    .withMetricType(MetricType.COSINE)
                    .withOutFields(List.of("doc_id", "chunk_index", ID_FIELD))
                    .withTopK(topK)
                    .withFloatVectors(List.of(queryVector))
                    .withVectorFieldName(VECTOR_FIELD)
                    .withConsistencyLevel(ConsistencyLevelEnum.EVENTUALLY)
                    .build();

            R<io.milvus.grpc.SearchResults> result = client.search(searchParam);
            if (result.getStatus() != 0 || result.getData() == null) {
                log.warn("Search failed for {}: {}", name, result.getMessage());
                return List.of();
            }

            SearchResultsWrapper wrapper = new SearchResultsWrapper(result.getData().getResults());
            List<SearchHit> hits = new ArrayList<>();
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
            for (SearchResultsWrapper.IDScore score : scores) {
                Map<String, Object> fields = score.getFieldValues();
                String chunkId = String.valueOf(fields.getOrDefault(ID_FIELD, ""));
                float s = score.getScore();
                Map<String, String> meta = new LinkedHashMap<>();
                meta.put("doc_id", String.valueOf(fields.getOrDefault("doc_id", "")));
                meta.put("chunk_index", String.valueOf(fields.getOrDefault("chunk_index", "0")));
                hits.add(new SearchHit(chunkId, s, meta));
            }
            return hits;
        } catch (Exception e) {
            log.error("Error searching {}: {}", name, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void deleteByDocId(String kbId, String docId) {
        String name = collectionName(kbId);
        try {
            client.delete(DeleteParam.newBuilder()
                    .withCollectionName(name)
                    .withExpr("doc_id == \"" + docId + "\"")
                    .build());
            log.info("Deleted vectors for doc {} from {}", docId, name);
        } catch (Exception e) {
            log.error("Error deleting vectors for doc {}: {}", docId, e.getMessage(), e);
        }
    }

    @Override
    public long count(String kbId) {
        String name = collectionName(kbId);
        try {
            R<io.milvus.grpc.QueryResults> result = client.query(io.milvus.param.dml.QueryParam.newBuilder()
                    .withCollectionName(name)
                    .withExpr(ID_FIELD + " != \"\"")
                    .withOutFields(List.of("count(*)"))
                    .build());
            if (result.getStatus() == 0 && result.getData() != null) {
                return result.getData().getFieldsDataCount() > 0 ? result.getData().getFieldsData(0).getScalars().getLongData().getData(0) : 0;
            }
        } catch (Exception e) {
            log.error("Error counting {}: {}", name, e.getMessage(), e);
        }
        return 0;
    }
}
