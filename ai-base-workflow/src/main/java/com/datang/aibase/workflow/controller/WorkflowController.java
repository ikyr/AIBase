package com.datang.aibase.workflow.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.workflow.entity.WfDefinition;
import com.datang.aibase.workflow.entity.WfInstance;
import com.datang.aibase.workflow.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ApiResponse<List<WfDefinition>> listAll() {
        return ApiResponse.ok(workflowService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<WfDefinition> getById(@PathVariable String id) {
        return ApiResponse.ok(workflowService.getById(id));
    }

    @PostMapping
    public ApiResponse<WfDefinition> create(@RequestBody WfDefinition definition) {
        return ApiResponse.ok(workflowService.create(definition));
    }

    @GetMapping("/{id}/dag")
    public ApiResponse<Map<String, Object>> getDag(@PathVariable String id) {
        return ApiResponse.ok(workflowService.parseDag(id));
    }

    @GetMapping("/instances")
    public ApiResponse<List<WfInstance>> listInstances(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(workflowService.listInstances(limit));
    }

    @GetMapping("/instances/{id}")
    public ApiResponse<WfInstance> getInstance(@PathVariable String id) {
        return ApiResponse.ok(workflowService.getInstance(id));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<WfInstance> start(@PathVariable String id, @RequestBody Map<String, Object> input) {
        return ApiResponse.ok(workflowService.start(id, input));
    }

    @PostMapping("/{id}")
    public ApiResponse<WfDefinition> update(@PathVariable String id, @RequestBody WfDefinition definition) {
        return ApiResponse.ok(workflowService.update(id, definition));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        workflowService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/instances/{id}/signal")
    public ApiResponse<WfInstance> signal(@PathVariable String id, @RequestBody Map<String, Object> signalData) {
        return ApiResponse.ok(workflowService.signal(id, signalData));
    }
}
