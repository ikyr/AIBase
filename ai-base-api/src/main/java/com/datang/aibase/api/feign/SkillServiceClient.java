package com.datang.aibase.api.feign;
import com.datang.aibase.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "skill-service", path = "/api/v1/skill")
public interface SkillServiceClient {
    @PostMapping("/execute/{skillId}")
    ApiResponse<Object> execute(@PathVariable String skillId, @RequestBody Map<String,Object> input);
    @GetMapping("/discover")
    ApiResponse<Object> discover(@RequestParam String query);
}
