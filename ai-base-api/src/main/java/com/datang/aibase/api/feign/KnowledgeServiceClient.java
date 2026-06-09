package com.datang.aibase.api.feign;
import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "knowledge-service", path = "/api/v1/knowledge")
public interface KnowledgeServiceClient {
    @PostMapping("/kb")
    ApiResponse<KbConfigInfo> createKb(@RequestBody KbCreateRequest request,
                                       @RequestHeader("X-User-Id") String userId,
                                       @RequestHeader("X-Dept-Id") String deptId);
    @GetMapping("/kb")
    ApiResponse<java.util.List<KbConfigInfo>> listKb(@RequestHeader("X-User-Id") String userId,
                                                      @RequestHeader("X-Dept-Id") String deptId);
    @PostMapping("/search")
    ApiResponse<SearchResult> search(@RequestBody SearchRequest request,
                                     @RequestHeader("X-User-Id") String userId,
                                     @RequestHeader("X-Dept-Id") String deptId);
    @PostMapping("/ingest")
    ApiResponse<IngestResult> ingest(@RequestBody IngestRequest request);
    @DeleteMapping("/{docId}")
    ApiResponse<Void> delete(@PathVariable String docId);
    @GetMapping("/kb/{kbId}/stats")
    ApiResponse<KnowledgeStats> stats(@PathVariable String kbId);
}
