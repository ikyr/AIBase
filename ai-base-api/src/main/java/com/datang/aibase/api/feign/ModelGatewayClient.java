package com.datang.aibase.api.feign;
import com.datang.aibase.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "model-gateway", path = "/api/v1/model")
public interface ModelGatewayClient {
    @PostMapping("/chat")
    ApiResponse<Object> chat(@RequestBody Map<String,Object> request);
    @PostMapping("/embed")
    ApiResponse<Object> embed(@RequestBody Map<String,Object> request);
}
