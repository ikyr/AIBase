package com.datang.aibase.model;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datang.aibase")
public class ModelGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelGatewayApplication.class, args);
    }
}
