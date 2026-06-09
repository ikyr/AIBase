package com.datang.aibase.knowledge.service;

import com.datang.aibase.knowledge.entity.KbDocument;
import com.datang.aibase.knowledge.model.KbInfo;
import java.util.List;
import java.util.Map;

public interface KbService {
    List<KbInfo> listAll();
    KbInfo getById(String id);
    List<com.datang.aibase.knowledge.model.KbDocument> getDocuments(String kbId);
    KbInfo create(String name, String description);
    KbInfo create(String name, String description, String kbType, int chunkSize, int chunkOverlap);
    KbDocument ingest(String kbId, String title, String content, String sourceType, String fileType);
    void deleteDocument(String docId);
    List<Map<String, Object>> search(String kbId, String query, int topK);
    List<Map<String, Object>> search(String kbId, String query, int topK, double threshold, String searchType);
    Map<String, Object> stats(String kbId);
}
