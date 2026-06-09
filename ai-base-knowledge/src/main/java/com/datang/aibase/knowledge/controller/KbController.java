package com.datang.aibase.knowledge.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.knowledge.entity.KbDocument;
import com.datang.aibase.knowledge.model.KbInfo;
import com.datang.aibase.knowledge.service.KbService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KbController {

    private final KbService kbService;

    public KbController(KbService kbService) {
        this.kbService = kbService;
    }

    @GetMapping("/kb")
    public ApiResponse<List<KbInfo>> listAll() {
        return ApiResponse.ok(kbService.listAll());
    }

    @GetMapping("/kb/{id}")
    public ApiResponse<KbInfo> getById(@PathVariable String id) {
        KbInfo kb = kbService.getById(id);
        if (kb == null) return ApiResponse.error("NOT_FOUND", "Knowledge base not found");
        return ApiResponse.ok(kb);
    }

    @GetMapping("/kb/{id}/documents")
    public ApiResponse<List<com.datang.aibase.knowledge.model.KbDocument>> getDocuments(@PathVariable String id) {
        return ApiResponse.ok(kbService.getDocuments(id));
    }

    @PostMapping("/kb")
    public ApiResponse<KbInfo> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.getOrDefault("description", "");
        String kbType = (String) body.getOrDefault("kbType", "PUBLIC");
        int chunkSize = body.containsKey("chunkSize") ? ((Number) body.get("chunkSize")).intValue() : 800;
        int chunkOverlap = body.containsKey("chunkOverlap") ? ((Number) body.get("chunkOverlap")).intValue() : 100;
        return ApiResponse.ok(kbService.create(name, description, kbType, chunkSize, chunkOverlap));
    }

    @PostMapping("/kb/{id}/ingest")
    public ApiResponse<KbDocument> ingest(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        String sourceType = (String) body.getOrDefault("sourceType", "UPLOAD");
        String fileType = (String) body.getOrDefault("fileType", "txt");
        return ApiResponse.ok(kbService.ingest(id, title, content, sourceType, fileType));
    }

    @PostMapping("/search")
    public ApiResponse<List<Map<String, Object>>> search(@RequestBody Map<String, Object> body) {
        String kbId = (String) body.get("kbId");
        String query = (String) body.get("query");
        int topK = body.containsKey("topK") ? ((Number) body.get("topK")).intValue() : 10;
        double threshold = body.containsKey("threshold") ? ((Number) body.get("threshold")).doubleValue() : 0.0;
        String searchType = (String) body.getOrDefault("searchType", "HYBRID");
        return ApiResponse.ok(kbService.search(kbId, query, topK, threshold, searchType));
    }

    @GetMapping("/kb/{id}/stats")
    public ApiResponse<Map<String, Object>> stats(@PathVariable String id) {
        return ApiResponse.ok(kbService.stats(id));
    }

    @DeleteMapping("/{docId}")
    public ApiResponse<Void> deleteDocument(@PathVariable String docId) {
        kbService.deleteDocument(docId);
        return ApiResponse.ok(null);
    }
}
