package com.datang.aibase.platform.controller;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.platform.entity.ApprovalRecord;
import com.datang.aibase.platform.entity.PromptVersion;
import com.datang.aibase.platform.service.PlatformService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/prompts")
    public ApiResponse<List<PromptVersion>> listPrompts() {
        return ApiResponse.ok(platformService.listPrompts());
    }

    @PostMapping("/prompts")
    public ApiResponse<PromptVersion> createPrompt(@RequestBody PromptVersion prompt) {
        return ApiResponse.ok(platformService.createPrompt(prompt));
    }

    @GetMapping("/approvals")
    public ApiResponse<List<ApprovalRecord>> listApprovals() {
        return ApiResponse.ok(platformService.listApprovals());
    }

    @PostMapping("/approvals")
    public ApiResponse<ApprovalRecord> createApproval(@RequestBody ApprovalRecord record) {
        return ApiResponse.ok(platformService.createApproval(record));
    }

    @PostMapping("/approvals/{id}/approve")
    public ApiResponse<ApprovalRecord> approve(@PathVariable String id) {
        return ApiResponse.ok(platformService.approve(id));
    }

    @PostMapping("/approvals/{id}/reject")
    public ApiResponse<ApprovalRecord> reject(@PathVariable String id, @RequestBody(required = false) java.util.Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ApiResponse.ok(platformService.reject(id, reason));
    }

    @PostMapping("/approvals/{id}/delegate")
    public ApiResponse<ApprovalRecord> delegate(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        String newApprover = body.get("approver");
        if (newApprover == null || newApprover.isBlank()) {
            throw new IllegalArgumentException("approver is required");
        }
        return ApiResponse.ok(platformService.delegate(id, newApprover));
    }

    @PostMapping("/prompts/{id}/publish")
    public ApiResponse<PromptVersion> publishPrompt(@PathVariable String id) {
        return ApiResponse.ok(platformService.publishPrompt(id));
    }

    @PostMapping("/prompts/rollback")
    public ApiResponse<PromptVersion> rollbackPrompt(@RequestBody java.util.Map<String, Object> body) {
        String refType = (String) body.get("refType");
        String refId = (String) body.get("refId");
        int targetVersion = body.get("targetVersion") != null ? ((Number) body.get("targetVersion")).intValue() : 0;
        if (refType == null || refId == null || targetVersion <= 0) {
            throw new IllegalArgumentException("refType, refId, and targetVersion are required");
        }
        return ApiResponse.ok(platformService.rollbackPrompt(refType, refId, targetVersion));
    }
}
