package com.datang.aibase.skill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.datang.aibase")
@EnableCaching
public class SkillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillApplication.class, args);
    }
}
