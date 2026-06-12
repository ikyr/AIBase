package com.datang.aibase.knowledge.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.knowledge.pipeline.DocumentParser;
import com.datang.aibase.knowledge.pipeline.DocumentParser.ParsedDocument;
import com.datang.aibase.knowledge.pipeline.IngestPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
public class DocumentUploadController {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadController.class);
    private final IngestPipeline ingestPipeline;
    private final DocumentParser documentParser;

    public DocumentUploadController(IngestPipeline ingestPipeline, DocumentParser documentParser) {
        this.ingestPipeline = ingestPipeline;
        this.documentParser = documentParser;
    }

    @PostMapping("/kb/{kbId}/upload")
    public ApiResponse<Map<String, Object>> upload(
            @PathVariable String kbId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ApiResponse.error("EMPTY_FILE", "Uploaded file is empty");
        }

        long maxSize = 50L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return ApiResponse.error("FILE_TOO_LARGE", "File exceeds 50MB limit");
        }

        try {
            byte[] bytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            ParsedDocument doc = documentParser.parse(bytes, fileName);

            String fileType = doc.mimeType().contains("/") ? doc.mimeType().split("/")[1] : "txt";
            var kbDoc = ingestPipeline.ingest(kbId, doc.title(), doc.content(), "UPLOAD", fileType);
            String docId = kbDoc.getId();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("docId", docId);
            result.put("title", doc.title());
            result.put("mimeType", doc.mimeType());
            result.put("size", file.getSize());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("Upload failed for kb {}: {}", kbId, e.getMessage());
            return ApiResponse.error("UPLOAD_FAILED", e.getMessage());
        }
    }
}
