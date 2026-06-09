package com.datang.aibase.model.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.model.entity.ModelCallLog;
import com.datang.aibase.model.entity.ModelConfig;
import com.datang.aibase.model.entity.ModelRouteRule;
import com.datang.aibase.model.service.ModelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/model")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public ApiResponse<List<ModelConfig>> listAll() {
        return ApiResponse.ok(modelService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ModelConfig> getById(@PathVariable String id) {
        return ApiResponse.ok(modelService.getById(id));
    }

    @PostMapping
    public ApiResponse<ModelConfig> create(@RequestBody ModelConfig config) {
        return ApiResponse.ok(modelService.create(config));
    }

    @GetMapping("/rules")
    public ApiResponse<List<ModelRouteRule>> listRules() {
        return ApiResponse.ok(modelService.listRules());
    }

    @PostMapping("/rules")
    public ApiResponse<ModelRouteRule> addRule(@RequestBody ModelRouteRule rule) {
        return ApiResponse.ok(modelService.addRule(rule));
    }

    @GetMapping("/logs")
    public ApiResponse<List<ModelCallLog>> listLogs(@RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(modelService.listLogs(limit));
    }

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(modelService.chat(request));
    }

    @PostMapping("/embed")
    public ApiResponse<Map<String, Object>> embed(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(modelService.embed(request));
    }
}
