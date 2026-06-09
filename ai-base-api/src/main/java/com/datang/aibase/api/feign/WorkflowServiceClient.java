package com.datang.aibase.api.feign;
import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "workflow-service", path = "/api/v1/workflow")
public interface WorkflowServiceClient {
    @PostMapping("/execute")
    ApiResponse<WorkflowResult> execute(@RequestBody WorkflowExecuteRequest request);
    @GetMapping("/instance/{instanceId}")
    ApiResponse<WorkflowResult> getInstance(@PathVariable String instanceId);
    @PostMapping("/instance/{instanceId}/cancel")
    ApiResponse<Void> cancel(@PathVariable String instanceId);
}
