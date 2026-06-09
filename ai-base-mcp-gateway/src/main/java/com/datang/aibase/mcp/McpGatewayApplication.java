package com.datang.aibase.mcp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datang.aibase")
@MapperScan({"com.datang.aibase.mcp.mapper", "com.datang.aibase.mcpgateway.mapper"})
public class McpGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpGatewayApplication.class, args);
    }
}
