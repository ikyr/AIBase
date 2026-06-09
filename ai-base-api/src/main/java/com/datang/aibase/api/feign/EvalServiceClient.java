package com.datang.aibase.api.feign;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.dto.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "eval-service", path = "/api/v1/eval")
public interface EvalServiceClient {

    @PostMapping("/datasets")
    ApiResponse<Map<String, Object>> createDataset(@RequestBody Map<String, Object> request);

    @GetMapping("/datasets")
    ApiResponse<PageResult<Map<String, Object>>> listDatasets(@RequestParam int page, @RequestParam int size);

    @GetMapping("/datasets/{datasetId}")
    ApiResponse<Map<String, Object>> getDataset(@PathVariable String datasetId);

    @DeleteMapping("/datasets/{datasetId}")
    ApiResponse<Void> deleteDataset(@PathVariable String datasetId);

    @PostMapping("/tasks")
    ApiResponse<Map<String, Object>> createTask(@RequestBody Map<String, Object> request);

    @GetMapping("/tasks")
    ApiResponse<PageResult<Map<String, Object>>> listTasks(@RequestParam int page, @RequestParam int size);

    @GetMapping("/tasks/{taskId}/results")
    ApiResponse<Map<String, Object>> getResults(@PathVariable String taskId);
}
