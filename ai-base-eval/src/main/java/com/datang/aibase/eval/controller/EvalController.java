package com.datang.aibase.eval.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.dto.PageResult;
import com.datang.aibase.eval.entity.*;
import com.datang.aibase.eval.service.EvalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eval")
public class EvalController {

    private final EvalService evalService;

    public EvalController(EvalService evalService) {
        this.evalService = evalService;
    }

    // Datasets
    @GetMapping("/datasets")
    public ApiResponse<List<EvalDataset>> listDatasets() {
        return ApiResponse.ok(evalService.listDatasets());
    }

    @GetMapping(value = "/datasets", params = {"page", "size"})
    public ApiResponse<PageResult<Map<String, Object>>> listDatasetsPaged(@RequestParam int page, @RequestParam int size) {
        List<EvalDataset> all = evalService.listDatasets();
        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<Map<String, Object>> items = all.subList(Math.min(start, all.size()), end).stream()
                .map(d -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", d.getId());
                    m.put("name", d.getName());
                    m.put("description", d.getDescription());
                    m.put("evalType", d.getEvalType());
                    m.put("itemCount", d.getItemCount());
                    m.put("status", d.getStatus());
                    m.put("createdAt", d.getCreatedAt());
                    return m;
                }).toList();
        return ApiResponse.ok(new PageResult<>(items, all.size(), page, size));
    }

    @GetMapping("/datasets/{id}")
    public ApiResponse<EvalDataset> getDataset(@PathVariable String id) {
        return ApiResponse.ok(evalService.getDataset(id));
    }

    @PostMapping("/datasets")
    public ApiResponse<EvalDataset> createDataset(@RequestBody EvalDataset dataset) {
        return ApiResponse.ok(evalService.createDataset(dataset));
    }

    @DeleteMapping("/datasets/{id}")
    public ApiResponse<Void> deleteDataset(@PathVariable String id) {
        evalService.deleteDataset(id);
        return ApiResponse.ok(null);
    }

    // Dataset items
    @GetMapping("/datasets/{id}/items")
    public ApiResponse<List<EvalDatasetItem>> getDatasetItems(@PathVariable String id) {
        return ApiResponse.ok(evalService.getDatasetItems(id));
    }

    @PostMapping("/datasets/{id}/items")
    public ApiResponse<EvalDatasetItem> addDatasetItem(@PathVariable String id, @RequestBody EvalDatasetItem item) {
        return ApiResponse.ok(evalService.addDatasetItem(id, item));
    }

    // Tasks
    @GetMapping("/tasks")
    public ApiResponse<List<EvalTask>> listTasks(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(evalService.listTasks(limit));
    }

    @GetMapping(value = "/tasks", params = {"page", "size"})
    public ApiResponse<PageResult<Map<String, Object>>> listTasksPaged(@RequestParam int page, @RequestParam int size) {
        List<EvalTask> all = evalService.listTasks(Integer.MAX_VALUE);
        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<Map<String, Object>> items = all.subList(Math.min(start, all.size()), end).stream()
                .map(t -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("datasetId", t.getDatasetId());
                    m.put("targetId", t.getTargetId());
                    m.put("targetType", t.getTargetType());
                    m.put("status", t.getStatus());
                    m.put("metrics", t.getMetrics());
                    m.put("totalItems", t.getTotalItems());
                    m.put("passedItems", t.getPassedItems());
                    m.put("startedAt", t.getStartedAt());
                    m.put("completedAt", t.getCompletedAt());
                    m.put("createdAt", t.getCreatedAt());
                    return m;
                }).toList();
        return ApiResponse.ok(new PageResult<>(items, all.size(), page, size));
    }

    @GetMapping("/tasks/{taskId}/results")
    public ApiResponse<Map<String, Object>> getResultsByTaskPath(@PathVariable String taskId) {
        List<EvalResult> results = evalService.getResults(taskId);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("results", results);
        result.put("total", results.size());
        return ApiResponse.ok(result);
    }

    @GetMapping("/tasks/{id}")
    public ApiResponse<EvalTask> getTask(@PathVariable String id) {
        return ApiResponse.ok(evalService.getTask(id));
    }

    @PostMapping("/tasks")
    public ApiResponse<EvalTask> createTask(@RequestBody EvalTask task) {
        return ApiResponse.ok(evalService.createTask(task));
    }

    @PostMapping("/tasks/{id}/execute")
    public ApiResponse<Map<String, Object>> executeTask(@PathVariable String id) {
        return ApiResponse.ok(evalService.executeTask(id));
    }

    // Results
    @GetMapping("/results/{taskId}")
    public ApiResponse<List<EvalResult>> getResults(@PathVariable String taskId) {
        return ApiResponse.ok(evalService.getResults(taskId));
    }

    // Annotations
    @GetMapping("/results/{resultId}/annotations")
    public ApiResponse<List<AnnotationRecord>> getAnnotations(@PathVariable String resultId) {
        return ApiResponse.ok(evalService.getAnnotations(resultId));
    }

    @PostMapping("/results/{resultId}/annotations")
    public ApiResponse<AnnotationRecord> addAnnotation(@PathVariable String resultId, @RequestBody AnnotationRecord annotation) {
        return ApiResponse.ok(evalService.addAnnotation(resultId, annotation));
    }
}
