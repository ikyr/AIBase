# AIBase Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build AIBase — an AI scaffolding platform based on Spring AI Alibaba, integrating knowledge base, Graph workflow orchestration, multi-agent coordination, Skill support, MCP services, model gateway, and evaluation framework across 8 microservices.

**Architecture:** Domain-driven microservices with Spring Cloud Alibaba. 8 independent services (knowledge, workflow, agent, skill, mcp-gateway, model-gateway, eval, platform) + API Gateway + common/api modules. Maven multi-module project. PostgreSQL, Redis, RocketMQ, Milvus as infrastructure.

**Tech Stack:** Java 21, Spring Boot 3.3, Spring AI Alibaba, Spring Cloud, Nacos, PostgreSQL, Redis, RocketMQ, Milvus, OpenTelemetry, Maven, MyBatis 3.0.3, Druid 1.2.20

---

## Implementation Progress (as of 2026-06-04)

### Completed (Core Wiring)

| Phase | Status | Notes |
|-------|--------|-------|
| Phase 0 | Done | Maven parent POM created, Docker Compose infrastructure YAML ready |
| Phase 1 | Done | ai-base-common: BaseEntity, SnowflakeIdGenerator, ApiResponse, enums, security filters, exception hierarchy, GlobalExceptionHandler |
| Phase 2 | Done | ai-base-api: Module POM created; Feign clients defined but ChatController is echo stub |
| Phase 3 | **Wired** | knowledge-service: CRUD + IngestPipeline + DocumentSplitter + FileSystemConnector done. EmbeddingService now calls model-gateway via RestClient. DashScopeSearchAdapter still stub. Milvus KnowledgeRepository has no implementation |
| Phase 4 | **Done** | model-gateway: Full CRUD + DashScopeProvider + OpenAIProvider with real HTTP calls + ModelRouter + REST controller. **/embed endpoint added** |
| Phase 5 | **Wired** | skill-service: CRUD done. **POST /api/v1/skill/execute endpoint added** — execute() currently returns skill config + context without calling LLM |
| Phase 6 | **Done** | workflow-service: CRUD + DagParser + **WorkflowExecutor** with Kahn's topological sort + **real SKILL/AGENT node execution via RestClient** + **WfNodeExec per-node tracking (V009 migration)** |
| Phase 7 | **Wired** | agent-service: CRUD + **ReActLoop** with tool registry + chat endpoint + **ModelGatewayClient** calling model-gateway via RestClient |
| Phase 8 | Partial | mcp-gateway: DB migration SQL created, CRUD done. No MCP protocol implementation (JSON-RPC, stdio/SSE) |
| Phase 9 | Partial | eval-service: DB migration SQL created, CRUD done. No evaluation runner — tasks/datasets are created but never executed |
| Phase 10 | Partial | platform-service: DB migration SQL created, CRUD done. approve()/reject() only set status, no workflow chain or notifications |
| Phase 11 | Partial | API Gateway: pom.xml + application.yml with 8 routes configured. No custom Java filters or auth |

### Key New Files Beyond Original Plan

**DB Migrations (Point 1):**
- `db/migrations/V001__knowledge.sql` through `V008__platform.sql` — All tables across 8 services
- `db/migrations/V009__wf_node_exec.sql` — Per-node execution tracking (NEW)

**Backend Business Logic (Point 3):**
- `ai-base-workflow/.../engine/WorkflowExecutor.java` — DAG execution via Kahn's algorithm + real SKILL/AGENT HTTP calls + WfNodeExec tracking
- `ai-base-workflow/.../entity/WfNodeExec.java` — Per-node execution record entity (NEW)
- `ai-base-workflow/.../mapper/WfNodeExecMapper.java` — MyBatis mapper for wf_node_exec (NEW)
- `ai-base-agent/.../engine/ReActLoop.java` — Reason-Act-Observe loop with tool registration
- `ai-base-agent/.../client/ModelGatewayClient.java` — RestClient calling model-gateway /chat (NEW)
- `ai-base-model-gateway/.../provider/DashScopeProvider.java` — Real HTTP calls to DashScope
- `ai-base-model-gateway/.../provider/OpenAIProvider.java` — Real HTTP calls to OpenAI-compatible APIs
- `ai-base-knowledge/.../pipeline/EmbeddingService.java` — Now calls model-gateway /embed via RestClient (NEW)

**Entity Fixes (Point 4):**
- `EvalResult.java` — Now extends BaseEntity
- `AnnotationRecord.java` — Now extends BaseEntity

**Frontend (Point 2):**
- 8 API modules (`src/shared/api/*.ts`) — Type-safe API clients for all services
- 8 Zustand stores — Converted from mock data to real API calls
- 17 pages updated — Field mappings aligned with backend entity types
- See [frontend plan](./2026-06-02-ai-base-frontend.md) for details

### Architecture Decisions Made During Implementation

1. **MyBatis annotations over JPA/Hibernate**: All mappers use `@Select`/`@Insert`/`@Update` annotations instead of Spring Data JPA repositories. No `*Repository.java` interfaces were created.
2. **Direct REST calls over Feign**: Frontend calls API Gateway directly via Axios; backend services use Spring `RestClient` for inter-service HTTP calls.
3. **Constructor injection over field injection**: All services use constructor-based DI per project coding standards.
4. **Service ports differ from original plan**:
   - knowledge=8101, skill=8102, workflow=8103, model-gateway=8104, agent=8105, mcp-gateway=8106, eval=8107, platform=8108, gateway=8081
5. **RestClient for inter-service calls**: `ModelGatewayClient` (agent→model-gateway), `EmbeddingService` (knowledge→model-gateway), `WorkflowExecutor` (workflow→skill-service + agent-service) all use `RestClient.create()` with configurable base URLs.
6. **Per-node execution tracking**: WorkflowExecutor records `WfNodeExec` rows (RUNNING→COMPLETED/FAILED) with timing and input/output for each DAG node.

---

## Phase 0: Project Scaffolding

### Task 0.1: Create Maven parent POM

**Files:**
- Create: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.datang</groupId>
    <artifactId>ai-base</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>AIBase</name>
    <description>AI scaffolding base platform based on Spring AI Alibaba</description>

    <modules>
        <module>ai-base-common</module>
        <module>ai-base-api</module>
        <module>ai-base-knowledge</module>
        <module>ai-base-workflow</module>
        <module>ai-base-agent</module>
        <module>ai-base-skill</module>
        <module>ai-base-mcp-gateway</module>
        <module>ai-base-model-gateway</module>
        <module>ai-base-eval</module>
        <module>ai-base-platform</module>
        <module>ai-base-gateway</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.3.0</spring-boot.version>
        <spring-cloud.version>2023.0.2</spring-cloud.version>
        <spring-cloud-alibaba.version>2023.0.1.0</spring-cloud-alibaba.version>
        <spring-ai-alibaba.version>1.0.0-M3</spring-ai-alibaba.version>
        <postgresql.version>42.7.3</postgresql.version>
        <milvus.version>2.4.1</milvus.version>
        <rocketmq.version>5.2.0</rocketmq.version>
        <opentelemetry.version>1.38.0</opentelemetry.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-ai-alibaba-starter</artifactId>
                <version>${spring-ai-alibaba.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 1: Initialize project root**

Run: Verify parent POM is created with all 11 modules declared.

### Task 0.2: Create application base YAML configuration

**Files:**
- Create: `ai-base-common/src/main/resources/application-base.yml`

```yaml
# Common application configuration for all services
spring:
  application:
    name: ${SERVICE_NAME:ai-base}
  datasource:
    url: jdbc:postgresql://${PG_HOST:localhost}:${PG_PORT:5432}/${PG_DB:aibase}
    username: ${PG_USER:aibase}
    password: ${PG_PASSWORD:aibase}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 10000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}
      config:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}
        file-extension: yml
  rocketmq:
    name-server: ${ROCKETMQ_NAMESRV:localhost:9876}
    producer:
      group: ${SERVICE_NAME:ai-base}-producer
    consumer:
      group: ${SERVICE_NAME:ai-base}-consumer

# Milvus
milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  username: ${MILVUS_USERNAME:}
  password: ${MILVUS_PASSWORD:}

# Observability
observability:
  logging:
    enabled: true
    level: INFO
    format: json
  tracing:
    enabled: true
    endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
    sampler: parentbased_traceidratio
    sample-rate: 1.0

# Knowledge base default config
knowledge:
  repository:
    type: milvus              # milvus / ragflow
  chunk:
    size: 800
    overlap: 100
  embedding:
    model: ${EMBEDDING_MODEL:text-embedding-v3}

# AI Model defaults
model:
  default-provider: dashscope
  dashscope:
    api-key: ${DASHSCOPE_API_KEY:}
    chat-model: qwen-plus
    embedding-model: text-embedding-v3

# Server
server:
  port: ${SERVER_PORT:8080}
  shutdown: graceful

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  otlp:
    tracing:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
  metrics:
    export:
      prometheus:
        enabled: true
```

### Task 0.3: Create Docker Compose for infrastructure

**Files:**
- Create: `docker-compose/infrastructure.yml`

```yaml
version: '3.8'
services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: aibase-pg
    environment:
      POSTGRES_DB: aibase
      POSTGRES_USER: aibase
      POSTGRES_PASSWORD: aibase
    ports:
      - "5432:5432"
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql

  redis:
    image: redis:7-alpine
    container_name: aibase-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  nacos:
    image: nacos/nacos-server:v2.3.2
    container_name: aibase-nacos
    environment:
      MODE: standalone
    ports:
      - "8848:8848"
      - "9848:9848"

  milvus:
    image: milvusdb/milvus:v2.4.1
    container_name: aibase-milvus
    ports:
      - "19530:19530"
      - "9091:9091"
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    depends_on:
      - etcd
      - minio

  etcd:
    image: quay.io/coreos/etcd:v3.5.5
    container_name: aibase-etcd
    environment:
      ETCD_AUTO_COMPACTION_MODE: revision
      ETCD_AUTO_COMPACTION_RETENTION: "1000"

  minio:
    image: minio/minio:latest
    container_name: aibase-minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"

  rocketmq:
    image: apache/rocketmq:5.2.0
    container_name: aibase-rocketmq
    ports:
      - "9876:9876"
      - "10911:10911"

  tempo:
    image: grafana/tempo:latest
    container_name: aibase-tempo
    command: ["-config.file=/etc/tempo/tempo.yaml"]
    volumes:
      - ./tempo.yaml:/etc/tempo/tempo.yaml
      - tempo_data:/var/tempo
    ports:
      - "4317:4317"
      - "3200:3200"

  loki:
    image: grafana/loki:latest
    container_name: aibase-loki
    ports:
      - "3100:3100"

  grafana:
    image: grafana/grafana:latest
    container_name: aibase-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  pg_data:
  redis_data:
  tempo_data:
  grafana_data:
```

- [ ] **Step 1: Verify Docker Compose**

Run: `cd docker-compose && docker compose -f infrastructure.yml config`
Expected: Configuration validated successfully.

---

## Phase 1: Common Module (ai-base-common)

### Task 1.1: Create common module POM

**Files:**
- Create: `ai-base-common/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.datang</groupId>
        <artifactId>ai-base</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>ai-base-common</artifactId>
    <name>AIBase Common</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-json</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

### Task 1.2: Create BaseEntity

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/entity/BaseEntity.java`

```java
package com.datang.aibase.common.entity;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 32)
    private String createdBy;

    @Column(name = "updated_by", length = 32)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = SnowflakeIdGenerator.nextId();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### Task 1.3: Create SnowflakeIdGenerator

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/util/SnowflakeIdGenerator.java`

```java
package com.datang.aibase.common.util;

public final class SnowflakeIdGenerator {

    private static final long EPOCH = 1700000000000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final SnowflakeIdGenerator INSTANCE = new SnowflakeIdGenerator(1, 1);

    private SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId out of range");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId out of range");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public static String nextId() {
        return String.valueOf(INSTANCE.nextIdLong());
    }

    private synchronized long nextIdLong() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

### Task 1.4: Create common enums

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/enums/ConfigStatus.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/enums/SourceType.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/enums/IngestStatus.java`

```java
// ConfigStatus.java
package com.datang.aibase.common.enums;

public enum ConfigStatus {
    ACTIVE, DISABLED, DELETED
}

// SourceType.java
package com.datang.aibase.common.enums;

public enum SourceType {
    UPLOAD, CONNECTOR, SEARCH, API
}

// IngestStatus.java
package com.datang.aibase.common.enums;

public enum IngestStatus {
    PENDING, SPLITTING, EMBEDDING, READY, FAILED
}
```

### Task 1.5: Create AI security filters

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/security/PromptInjectionDetector.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/security/SensitiveDataMasker.java`

```java
// PromptInjectionDetector.java
package com.datang.aibase.common.security;

import java.util.List;
import java.util.regex.Pattern;

public final class PromptInjectionDetector {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?(previous|above)\\s+(instructions?|prompts?)"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+.*\\s+(instead|not)"),
            Pattern.compile("(?i)pretend\\s+(you\\s+are|to\\s+be)"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?(your\\s+)?(training|instructions?|rules?)"),
            Pattern.compile("(?i)system\\s*:\\s*.*new\\s+instructions?"),
            Pattern.compile("(?i)\\{output\\s*=\\s*.*\\}")
    );

    private PromptInjectionDetector() {}

    public static boolean detectInjection(String input) {
        if (input == null || input.isBlank()) return false;
        return INJECTION_PATTERNS.stream().anyMatch(p -> p.matcher(input).find());
    }

    public static String sanitize(String input) {
        if (input == null) return null;
        String sanitized = input;
        for (Pattern pattern : INJECTION_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("[FILTERED]");
        }
        return sanitized;
    }
}

// SensitiveDataMasker.java
package com.datang.aibase.common.security;

import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\d{17}[\\dXx]");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(sk-|api[_-]?key[=:]\\s*)[A-Za-z0-9_\\-]{20,}");

    private SensitiveDataMasker() {}

    public static String mask(String text) {
        if (text == null || text.isBlank()) return text;
        String result = PHONE_PATTERN.matcher(text).replaceAll(m -> maskPhone(m.group()));
        result = ID_CARD_PATTERN.matcher(result).replaceAll(m -> maskIdCard(m.group()));
        result = API_KEY_PATTERN.matcher(result).replaceAll("[API_KEY_REDACTED]");
        return result;
    }

    private static String maskPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private static String maskIdCard(String idCard) {
        return idCard.substring(0, 4) + "**********" + idCard.substring(14);
    }
}
```

### Task 1.6: Create ApiResponse wrapper

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/dto/ApiResponse.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/dto/PageResult.java`

```java
// ApiResponse.java
package com.datang.aibase.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String errorCode;
    private String errorMsg;
    private String traceId;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> error(String code, String msg) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = false;
        resp.errorCode = code;
        resp.errorMsg = msg;
        return resp;
    }
}

// PageResult.java
package com.datang.aibase.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
}
```

### Task 1.7: Create exception hierarchy

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/exception/AiBaseException.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/exception/KnowledgeException.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/exception/WorkflowException.java`
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/exception/AgentException.java`

```java
// AiBaseException.java
package com.datang.aibase.common.exception;

import lombok.Getter;

@Getter
public abstract class AiBaseException extends RuntimeException {
    private final String errorCode;

    protected AiBaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AiBaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

// KnowledgeException.java
package com.datang.aibase.common.exception;

public class KnowledgeException extends AiBaseException {
    public KnowledgeException(String errorCode, String message) {
        super("KB_" + errorCode, message);
    }
}

// WorkflowException.java
package com.datang.aibase.common.exception;

public class WorkflowException extends AiBaseException {
    public WorkflowException(String errorCode, String message) {
        super("WF_" + errorCode, message);
    }
}

// AgentException.java
package com.datang.aibase.common.exception;

public class AgentException extends AiBaseException {
    public AgentException(String errorCode, String message) {
        super("AG_" + errorCode, message);
    }
}
```

### Task 1.8: Create global exception handler

**Files:**
- Create: `ai-base-common/src/main/java/com/datang/aibase/common/handler/GlobalExceptionHandler.java`

```java
package com.datang.aibase.common.handler;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.exception.AiBaseException;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(AiBaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAiBaseException(AiBaseException e) {
        log.error("AIBase exception: code={}, msg={}", e.getErrorCode(), e.getMessage(), e);
        ApiResponse<Void> resp = ApiResponse.error(e.getErrorCode(), e.getMessage());
        resp.setTraceId(tracer.currentSpan() != null
                ? tracer.currentSpan().context().traceId() : null);
        return resp;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknownException(Exception e) {
        log.error("Unknown exception", e);
        ApiResponse<Void> resp = ApiResponse.error("UNKNOWN", "Internal server error");
        resp.setTraceId(tracer.currentSpan() != null
                ? tracer.currentSpan().context().traceId() : null);
        return resp;
    }
}
```

- [ ] **Step 1: Verify common module compiles**

Run: `mvn clean compile -pl ai-base-common`
Expected: BUILD SUCCESS

---

## Phase 2: API Module (ai-base-api)

### Task 2.1: Create API module POM

**Files:**
- Create: `ai-base-api/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.datang</groupId>
        <artifactId>ai-base</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>ai-base-api</artifactId>
    <name>AIBase API</name>

    <dependencies>
        <dependency>
            <groupId>com.datang</groupId>
            <artifactId>ai-base-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Task 2.2: Create knowledge-service Feign client

**Files:**
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/KnowledgeServiceClient.java`

```java
package com.datang.aibase.api.feign;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.dto.PageResult;
import com.datang.aibase.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "knowledge-service", path = "/api/v1/knowledge")
public interface KnowledgeServiceClient {

    @PostMapping("/search")
    ApiResponse<SearchResult> search(@RequestBody SearchRequest request);

    @PostMapping("/ingest")
    ApiResponse<IngestResult> ingest(@RequestBody IngestRequest request);

    @DeleteMapping("/{docId}")
    ApiResponse<Void> delete(@PathVariable String docId);

    @GetMapping("/kb/{kbId}/stats")
    ApiResponse<KnowledgeStats> stats(@PathVariable String kbId);
}
```

### Task 2.3: Create agent-service Feign client

**Files:**
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/AgentServiceClient.java`

```java
package com.datang.aibase.api.feign;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.api.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "agent-service", path = "/api/v1/agent")
public interface AgentServiceClient {

    @PostMapping("/chat")
    ApiResponse<AgentChatResponse> chat(@RequestBody AgentChatRequest request);

    @PostMapping("/negotiate")
    ApiResponse<NegotiationResult> negotiate(@RequestBody NegotiationRequest request);

    @GetMapping("/session/{sessionId}")
    ApiResponse<SessionDetail> getSession(@PathVariable String sessionId);

    @GetMapping("/session/{sessionId}/messages")
    ApiResponse<PageResult<AgentMessage>> getMessages(@PathVariable String sessionId,
                                                       @RequestParam int page,
                                                       @RequestParam int size);
}
```

### Task 2.4: Create workflow, skill, mcp, model, eval Feign clients

**Files:**
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/WorkflowServiceClient.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/SkillServiceClient.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/McpGatewayClient.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/ModelGatewayClient.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/feign/EvalServiceClient.java`

### Task 2.5: Create API DTOs

**Files:**
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/SearchRequest.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/SearchResult.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/AgentChatRequest.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/AgentChatResponse.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/WorkflowExecuteRequest.java`
- Create: `ai-base-api/src/main/java/com/datang/aibase/api/dto/WorkflowResult.java`

---

## Phase 3: knowledge-service

### Task 3.1: Create knowledge-service module structure

**Files:**
- Create: `ai-base-knowledge/pom.xml`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/KnowledgeServiceApplication.java`

```java
package com.datang.aibase.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class KnowledgeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}
```

- Create: `ai-base-knowledge/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: knowledge-service
  config:
    import:
      - classpath:application-base.yml
server:
  port: 8101
```

### Task 3.2: Implement KnowledgeRepository interface and Milvus implementation

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/KnowledgeRepository.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/MilvusKnowledgeRepository.java`

```java
// KnowledgeRepository.java
package com.datang.aibase.knowledge.repository;

import com.datang.aibase.knowledge.model.*;

public interface KnowledgeRepository {
    IngestResult ingest(Document doc, IngestOptions options);
    void delete(String docId);
    SearchResult search(SearchRequest request);
    KnowledgeStats stats(String kbId);
}

// MilvusKnowledgeRepository.java
package com.datang.aibase.knowledge.repository;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.*;
import io.milvus.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "knowledge.repository.type", havingValue = "milvus", matchIfMissing = true)
public class MilvusKnowledgeRepository implements KnowledgeRepository {

    private final MilvusServiceClient milvusClient;

    @Override
    public SearchResult search(SearchRequest request) {
        // ANN search via Milvus gRPC
        // ...implementation
        return new SearchResult();
    }

    @Override
    public IngestResult ingest(Document doc, IngestOptions options) {
        // Split → Embed → Insert into Milvus collection
        // ...implementation
        return new IngestResult();
    }

    @Override
    public void delete(String docId) {
        // Delete vectors by docId filter expression
    }

    @Override
    public KnowledgeStats stats(String kbId) {
        // Get collection statistics
        return new KnowledgeStats();
    }
}
```

### Task 3.3: Implement RAGFlow adapter

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/RagFlowKnowledgeRepository.java`

```java
package com.datang.aibase.knowledge.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "knowledge.repository.type", havingValue = "ragflow")
public class RagFlowKnowledgeRepository implements KnowledgeRepository {

    private final RestClient restClient;
    private final RagFlowProperties ragFlowProperties;

    @Override
    public SearchResult search(SearchRequest request) {
        String response = restClient.post()
                .uri("/api/v1/datasets/{datasetId}/chunks/search", request.getKbId())
                .body(request)
                .retrieve()
                .body(String.class);
        return parseResponse(response);
    }

    @Override
    public IngestResult ingest(Document doc, IngestOptions options) {
        String response = restClient.post()
                .uri("/api/v1/datasets/{datasetId}/documents", doc.getKbId())
                .body(doc)
                .retrieve()
                .body(String.class);
        return parseIngestResult(response);
    }

    // ... other methods
}
```

### Task 3.4: Implement document processing pipeline

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/pipeline/DocumentSplitter.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/pipeline/EmbeddingService.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/pipeline/IngestPipeline.java`

### Task 3.5: Implement data source connectors

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/connector/DataSourceConnector.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/connector/FileSystemConnector.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/connector/DatabaseConnector.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/connector/UploadConnector.java`

### Task 3.6: Implement search engine adapters

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/search/SearchEngineAdapter.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/search/DashScopeSearchAdapter.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/search/TavilySearchAdapter.java`

### Task 3.7: Create JPA entities

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/entity/KbConfig.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/entity/KbDocument.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/entity/KbChunk.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/entity/ConnectorConfig.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/entity/SearchEngineConfig.java`

(Entity code as defined in design spec §knowledge-service)

### Task 3.8: Create Spring Data JPA repositories

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/KbConfigRepository.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/KbDocumentRepository.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/repository/KbChunkRepository.java`

### Task 3.9: Create service layer

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/service/KnowledgeService.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/service/IngestService.java`
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/service/SearchService.java`

### Task 3.10: Create REST controller

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/controller/KnowledgeController.java`

```java
package com.datang.aibase.knowledge.controller;

import com.datang.aibase.api.dto.*;
import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.knowledge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController implements KnowledgeServiceClient {

    private final SearchService searchService;
    private final IngestService ingestService;

    @Override
    @PostMapping("/search")
    public ApiResponse<SearchResult> search(@RequestBody SearchRequest request) {
        return ApiResponse.ok(searchService.search(request));
    }

    @Override
    @PostMapping("/ingest")
    public ApiResponse<IngestResult> ingest(@RequestBody IngestRequest request) {
        return ApiResponse.ok(ingestService.ingest(request));
    }

    @Override
    @DeleteMapping("/{docId}")
    public ApiResponse<Void> delete(@PathVariable String docId) {
        ingestService.delete(docId);
        return ApiResponse.ok(null);
    }

    @Override
    @GetMapping("/kb/{kbId}/stats")
    public ApiResponse<KnowledgeStats> stats(@PathVariable String kbId) {
        return ApiResponse.ok(searchService.getStats(kbId));
    }
}
```

### Task 3.11: Create Milvus configuration

**Files:**
- Create: `ai-base-knowledge/src/main/java/com/datang/aibase/knowledge/config/MilvusConfig.java`

```java
package com.datang.aibase.knowledge.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Bean
    @ConfigurationProperties(prefix = "milvus")
    public MilvusProperties milvusProperties() {
        return new MilvusProperties();
    }

    @Bean
    public MilvusServiceClient milvusClient(MilvusProperties props) {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(props.getHost())
                .withPort(props.getPort())
                .withAuthorization(props.getUsername(), props.getPassword())
                .build();
        return new MilvusServiceClient(connectParam);
    }
}
```

### Task 3.12: Write knowledge-service tests

**Files:**
- Create: `ai-base-knowledge/src/test/java/com/datang/aibase/knowledge/repository/MilvusKnowledgeRepositoryTest.java`
- Create: `ai-base-knowledge/src/test/java/com/datang/aibase/knowledge/service/SearchServiceTest.java`
- Create: `ai-base-knowledge/src/test/java/com/datang/aibase/knowledge/controller/KnowledgeControllerTest.java`

---

## Phase 4: model-gateway

### Task 4.1: Create module structure

**Files:**
- Create: `ai-base-model-gateway/pom.xml`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/ModelGatewayApplication.java`
- Create: `ai-base-model-gateway/src/main/resources/application.yml`

### Task 4.2: Create JPA entities

**Files:**
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/entity/ModelConfig.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/entity/ModelRouteRule.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/entity/ModelCallLog.java`

### Task 4.3: Implement model routing engine

**Files:**
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/router/ModelRouter.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/router/RoutingStrategy.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/provider/DashScopeProvider.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/provider/OpenAIProvider.java`

### Task 4.4: Create controller and service

**Files:**
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/service/ModelGatewayService.java`
- Create: `ai-base-model-gateway/src/main/java/com/datang/aibase/model/controller/ModelGatewayController.java`

---

## Phase 5: skill-service

### Task 5.1: Create module structure

**Files:**
- Create: `ai-base-skill/pom.xml`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/SkillServiceApplication.java`
- Create: `ai-base-skill/src/main/resources/application.yml`

### Task 5.2: Create JPA entities

**Files:**
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/entity/SkillDef.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/entity/SkillVersion.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/entity/SkillExecutionLog.java`

### Task 5.3: Implement three-layer skill execution

**Files:**
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/registry/SkillRegistry.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/executor/PromptSkillExecutor.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/executor/FunctionSkillExecutor.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/executor/AgentSkillExecutor.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/discovery/SkillDiscovery.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/sandbox/GraalJsSandbox.java`

### Task 5.4: Create controller and service

**Files:**
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/service/SkillService.java`
- Create: `ai-base-skill/src/main/java/com/datang/aibase/skill/controller/SkillController.java`

---

## Phase 6: workflow-service

### Task 6.1: Create module structure

**Files:**
- Create: `ai-base-workflow/pom.xml`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/WorkflowServiceApplication.java`
- Create: `ai-base-workflow/src/main/resources/application.yml`

### Task 6.2: Create JPA entities

**Files:**
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/entity/WfDefinition.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/entity/WfInstance.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/entity/WfNodeExec.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/entity/WfTemplate.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/entity/ApprovalTask.java`

### Task 6.3: Implement DAG workflow engine

**Files:**
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/WorkflowEngine.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/DagParser.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/DagExecutor.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/AgentNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/SkillNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/ToolNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/KnowledgeNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/ConditionNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/ParallelNode.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/engine/node/WaitNode.java`

### Task 6.4: Implement HITL approval workflow

**Files:**
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/approval/ApprovalService.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/approval/SlaTracker.java`

### Task 6.5: Create controller and service

**Files:**
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/service/WorkflowService.java`
- Create: `ai-base-workflow/src/main/java/com/datang/aibase/workflow/controller/WorkflowController.java`

---

## Phase 7: agent-service

### Task 7.1: Create module structure

**Files:**
- Create: `ai-base-agent/pom.xml`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/AgentServiceApplication.java`
- Create: `ai-base-agent/src/main/resources/application.yml`

### Task 7.2: Create JPA entities

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/entity/AgentDef.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/entity/AgentSession.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/entity/AgentMessage.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/entity/AgentNegotiation.java`

### Task 7.3: Implement ReAct Loop engine

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/runtime/AgentRuntime.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/runtime/ReActLoop.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/runtime/ToolRegistry.java`

### Task 7.4: Implement Graph Bridge

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/bridge/GraphBridge.java`

### Task 7.5: Implement Negotiation Engine

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/negotiation/NegotiationEngine.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/negotiation/NegotiationProtocol.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/negotiation/DebateStrategy.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/negotiation/VotingStrategy.java`

### Task 7.6: Implement Memory Manager

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/memory/MemoryManager.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/memory/ContextWindowManager.java`

### Task 7.7: Create controller and service

**Files:**
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/service/AgentService.java`
- Create: `ai-base-agent/src/main/java/com/datang/aibase/agent/controller/AgentController.java`

---

## Phase 8: mcp-gateway

### Task 8.1: Create module structure

**Files:**
- Create: `ai-base-mcp-gateway/pom.xml`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/McpGatewayApplication.java`
- Create: `ai-base-mcp-gateway/src/main/resources/application.yml`

### Task 8.2: Create JPA entities

**Files:**
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/entity/McpServerReg.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/entity/McpClientConn.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/entity/McpToolReg.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/entity/McpAudit.java`

### Task 8.3: Implement MCP Client

**Files:**
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/client/ConnectionPool.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/client/ProtocolAdapter.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/client/ToolProxy.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/client/McpClientService.java`

### Task 8.4: Implement MCP Server

**Files:**
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/server/CapabilityExporter.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/server/TransportLayer.java`
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/server/McpServerEndpoint.java`

### Task 8.5: Create controller

**Files:**
- Create: `ai-base-mcp-gateway/src/main/java/com/datang/aibase/mcp/controller/McpGatewayController.java`

---

## Phase 9: eval-service

### Task 9.1: Create module structure

**Files:**
- Create: `ai-base-eval/pom.xml`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/EvalServiceApplication.java`
- Create: `ai-base-eval/src/main/resources/application.yml`

### Task 9.2: Create JPA entities

**Files:**
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/entity/EvalDataset.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/entity/EvalDatasetItem.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/entity/EvalTask.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/entity/EvalResult.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/entity/AnnotationRecord.java`

### Task 9.3: Implement evaluation engine

**Files:**
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/engine/RagEvaluator.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/engine/AgentEvaluator.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/service/EvalService.java`
- Create: `ai-base-eval/src/main/java/com/datang/aibase/eval/controller/EvalController.java`

---

## Phase 10: platform-service

### Task 10.1: Create module structure

**Files:**
- Create: `ai-base-platform/pom.xml`
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/PlatformServiceApplication.java`
- Create: `ai-base-platform/src/main/resources/application.yml`

### Task 10.2: Create JPA entities

**Files:**
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/entity/PromptVersion.java`

### Task 10.3: Implement Prompt management

**Files:**
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/prompt/PromptManager.java`
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/prompt/PromptVersionService.java`
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/controller/PromptController.java`

### Task 10.4: Implement configuration management

**Files:**
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/config/ConfigManagementService.java`
- Create: `ai-base-platform/src/main/java/com/datang/aibase/platform/controller/ConfigController.java`

---

## Phase 11: API Gateway

### Task 11.1: Create API Gateway

**Files:**
- Create: `ai-base-gateway/pom.xml`
- Create: `ai-base-gateway/src/main/java/com/datang/aibase/gateway/ApiGatewayApplication.java`
- Create: `ai-base-gateway/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: knowledge-service
          uri: lb://knowledge-service
          predicates:
            - Path=/api/v1/knowledge/**
        - id: workflow-service
          uri: lb://workflow-service
          predicates:
            - Path=/api/v1/workflow/**
        - id: agent-service
          uri: lb://agent-service
          predicates:
            - Path=/api/v1/agent/**
        - id: skill-service
          uri: lb://skill-service
          predicates:
            - Path=/api/v1/skill/**
        - id: mcp-gateway
          uri: lb://mcp-gateway
          predicates:
            - Path=/api/v1/mcp/**
        - id: model-gateway
          uri: lb://model-gateway
          predicates:
            - Path=/api/v1/model/**
        - id: eval-service
          uri: lb://eval-service
          predicates:
            - Path=/api/v1/eval/**
        - id: platform-service
          uri: lb://platform-service
          predicates:
            - Path=/api/v1/platform/**
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
```

---

## Execution Order

```
Phase 0  ✅ Project scaffolding, Maven parent POM, Docker Compose
Phase 1  ✅ Common module (shared entities, utils, security, exception handling)
Phase 2  🔶 API module (Feign client interfaces defined; ChatController is echo stub)
Phase 3  🔶 knowledge-service (DB + CRUD + IngestPipeline + EmbeddingService wired; DashScope search stub; no Milvus impl)
Phase 4  ✅ model-gateway (DB + CRUD + real HTTP providers + /chat + /embed endpoints)
Phase 5  🔶 skill-service (DB + CRUD + /execute endpoint added; execute() returns config, no LLM call)
Phase 6  ✅ workflow-service (DB + CRUD + WorkflowExecutor + real SKILL/AGENT calls + WfNodeExec tracking)
Phase 7  ✅ agent-service (DB + CRUD + ReActLoop + ModelGatewayClient wired to model-gateway)
Phase 8  🔶 mcp-gateway (DB + CRUD done; no MCP protocol — JSON-RPC, stdio/SSE)
Phase 9  🔶 eval-service (DB + CRUD done; no evaluation runner)
Phase 10 🔶 platform-service (DB + CRUD done; approve/reject only sets status, no workflow)
Phase 11 🔶 API Gateway (pom.yml + routes config done; no custom filters/auth)
```

✅ = Core complete  🔶 = Partial (core done, advanced features pending)

## Remaining Work (Priority Order)

| Priority | Module | Work Needed |
|----------|--------|-------------|
| P0 | **Tests** | Zero test files exist. All 8 services need unit + integration tests (80% target) |
| P1 | **Skill** | `execute()` should call model-gateway with promptTemplate + context for real LLM execution |
| P1 | **Eval** | Implement evaluation runner: iterate dataset items, call target service, compute metrics |
| P1 | **Knowledge** | Implement MilvusKnowledgeRepository (Milvus SDK); replace DashScopeSearchAdapter stub |
| P2 | **MCP Gateway** | Implement MCP protocol: JSON-RPC transport, tool proxy, connection pool |
| P2 | **Platform** | Multi-step approval chains, notifications, prompt publish/rollback |
| P2 | **ai-base-api** | Wire ChatController to Feign clients for aggregated chat flow |
| P3 | **API Gateway** | Auth filter, rate limiting, request logging |
| P3 | **Agent** | GraphBridge for multi-agent orchestration, NegotiationEngine |
| P3 | **Workflow** | Additional node types (CONDITION, PARALLEL, WAIT), HITL approval integration |

Phases 3-9 have no hard dependencies between them once Phases 0-2 are complete, and can be built in parallel.
