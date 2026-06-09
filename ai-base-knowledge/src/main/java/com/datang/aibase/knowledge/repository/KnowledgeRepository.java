package com.datang.aibase.knowledge.repository;

import java.util.List;
import java.util.Map;

public interface KnowledgeRepository {

    void createCollection(String kbId, int dimension);

    void dropCollection(String kbId);

    void insertVectors(String kbId, List<String> ids, List<List<Float>> vectors, List<Map<String, String>> metadata);

    List<SearchHit> search(String kbId, List<Float> queryVector, int topK);

    void deleteByDocId(String kbId, String docId);

    long count(String kbId);

    record SearchHit(String id, float score, Map<String, String> metadata) {}
}
