package com.datang.aibase.eval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.datang.aibase")
public class EvalApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvalApplication.class, args);
    }
}
