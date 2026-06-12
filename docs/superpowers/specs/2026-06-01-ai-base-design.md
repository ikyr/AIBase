# AI 底座（AIBase）设计规格

## 概述

基于 Spring AI Alibaba 框架建设通用 AI 底座，支撑多场景上层 Agent 服务建设。底座集成知识库、Graph 工作流编排、多 Agent 协调、Skill 支持、MCP 服务支持，采用领域驱动微服务架构。

## 技术栈

| 组件 | 选型 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot + Spring Cloud + Spring Cloud Alibaba | 3.3.0 / 2023.0.2 / 2023.0.1.0 |
| 持久层 | MyBatis + Druid | 3.0.3 / 1.2.20 |
| AI 框架 | Spring AI Alibaba | 1.0.0-M3 |
| 注册/配置中心 | Nacos | 2.3.2 |
| 数据库 | PostgreSQL + pgvector | 16 |
| 缓存 | Redis | 7 |
| 消息队列 | RocketMQ | 5.2.0 |
| 向量数据库 | Milvus | 2.4.1 |
| 对象存储 | MinIO | 最新 |
| 可观测性 | OpenTelemetry + Grafana Tempo（链路追踪）+ Loki（日志）+ Grafana（看板） | 最新 |
| 编译环境 | Java 24（运行） / Java 21（编译目标）+ Maven 3.6.3 | — |

---

## 服务架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     API Gateway (Spring Cloud Gateway)           │
├────────┬────────┬────────┬────────┬────────┬────────┬──────────┤
│knowledge│workflow│ agent  │ skill  │mcp-gw  │ model  │   eval   │
│ 知识库  │ 工作流  │ Agent  │ Skill  │MCP网关 │ 模型   │  评估    │
│ service │ service│ service│ service│        │gateway │ service  │
├────────┴────────┴────────┴────────┴────────┴────────┴──────────┤
│                     platform-service (管理后台)                   │
│                     + Prompt管理中心 + 审批工作台                  │
├─────────────────────────────────────────────────────────────────┤
│  可观测性: OpenTelemetry + Tempo + Loki + Grafana (全链路TraceID) │
│  公共模块: ai-base-common (DTO/异常/工具/AI安全过滤器)           │
│  接口模块: ai-base-api (Feign接口定义/服务间契约)                 │
├─────────────────────────────────────────────────────────────────┤
│  Nacos / PG / Redis / RocketMQ / Milvus                          │
└─────────────────────────────────────────────────────────────────┘
```

---
## 模块职责总览

| 模块 | 端口 | 核心职责 | 关键实体 |
|------|------|---------|---------|
| **knowledge** | 8101 | 知识库抽象接口 + Milvus默认实现 + 文档摄取管线（Split→Embed→Store）+ 多检索策略（向量/关键词/混合） | KbConfig, KbDocument, KbChunk |
| **skill** | 8102 | 三层Skill体系（Prompt模板→Function函数→Agent子代理）注册/发现/热加载/版本管理 | SkillDef, SkillVersion, SkillExecutionLog |
| **workflow** | 8103 | Graph DAG工作流定义/执行/监控 + Human-in-the-Loop审批 + 9种节点类型 | WfDefinition, WfInstance |
| **model-gateway** | 8104 | 多模型统一接入（DashScope/OpenAI/本地）+ 智能路由 + Fallback降级 + 成本追踪 | ModelConfig, ModelRouteRule, ModelCallLog |
| **agent** | 8105 | Agent生命周期管理 + ReAct/Graph Bridge/Negotiation三模式协调 + 对话树分支上下文管理 | AgentDef, AgentSession, AgentMessage |
| **mcp-gateway** | 8106 | MCP Client连接池管理（SSE/Streamable HTTP双协议）+ MCP Server工具导出 | McpServerReg, McpClientConn, McpToolReg, McpAudit |
| **eval** | 8107 | RAG评估（MRR/NDCG）+ Agent评估（任务成功率/工具调用准确率）+ 回归测试集 + 人工标注 | EvalDataset, EvalTask, EvalResult, AnnotationRecord |
| **platform** | 8108 | 管理后台 + Prompt版本管理中心 + 审批工作台 + Skill市场 | PromptVersion |
| **gateway** | 8081 | API网关，8条路由通过 Docker DNS 转发（Docker Compose 服务名），全局 CORS 配置。Nacos Discovery 已禁用（gRPC 跨容器通信限制），改为静态 URI 路由 | — |

### 公共模块

| 模块 | 核心内容 |
|------|---------|
| **ai-base-common** | BaseEntity（MyBatis POJO基类，拦截器自动SnowflakeId+时间戳）、ApiResponse<T>（统一响应信封）、GlobalExceptionHandler（TraceID注入）、PromptInjectionDetector（注入检测）、SensitiveDataMasker（手机号/身份证/API Key脱敏）、SnowflakeIdGenerator（雪花算法单例）、SnowflakeAutoFillInterceptor（MyBatis拦截器）、枚举类 |
| **ai-base-api** | 5个Feign接口（Knowledge/Workflow/Agent/Skill/ModelGateway）+ 14个DTO（请求/响应/统计）+ 服务间契约定义 |

## 模块详细设计

### 1. knowledge-service（知识库服务）

**职责**：知识库抽象接口 + Milvus默认实现 + 数据源连接器

**核心抽象**：

```java
public interface KnowledgeRepository {
    String ingest(Document doc, IngestOptions options);
    void delete(String docId);
    SearchResult search(SearchRequest request);
    KnowledgeStats stats(String kbId);
}
```

**文档处理管线**：Ingest → Split（语义切分）→ Embed（向量化）→ Store（Milvus）

**检索策略**：向量检索（ANN）、混合检索（向量+关键词）、重排序（Reranker）、多路召回+融合

**知识库两大数据来源**：

| 来源 | 说明 | 输入方式 |
|------|------|---------|
| 内部文档上传 | 企业自有文档、手册、制度、历史工单等 | Web界面上传 + API批量导入 + 文件系统/数据库连接器同步 |
| 网络搜索 | 互联网实时信息检索，补充知识库覆盖盲区 | 搜索引擎API集成，实时检索+可选持久化 |

**内部文档上传 — 连接器框架**：

```java
public interface DataSourceConnector {
    String getName();
    List<Document> fetch(ConnectorConfig config);
    boolean supportIncremental();
    List<Document> fetchDelta(ConnectorConfig config, String lastSyncToken);
}
```

内置连接器：

| 连接器 | 数据源 | 场景 |
|--------|--------|------|
| fs-connector | 本地文件系统 / S3 / MinIO | 文档批量导入 |
| db-connector | PG / MySQL | 结构化数据转知识 |
| upload-connector | Web界面上传（PDF/Word/MD/TXT） | 用户手动上传 |
| api-connector | 第三方API（飞书文档/Confluence/Notion） | 外部系统同步 |

**外部知识库适配（RAGFlow等）**：

底座支持对接外部知识库系统，作为 KnowledgeRepository 的扩展实现。一期支持 RAGFlow。

```java
// RAGFlow 适配器 — 将 RAGFlow 的知识库能力封装为标准 KnowledgeRepository
public class RagFlowKnowledgeRepository implements KnowledgeRepository {

    private final String ragFlowEndpoint;   // RAGFlow API 端点
    private final String apiKey;            // RAGFlow API Key
    private final RestClient restClient;

    @Override
    public String ingest(Document doc, IngestOptions options) {
        // 调用 RAGFlow 文档上传 + 解析 API
        // POST /api/v1/datasets/{dataset_id}/documents
    }

    @Override
    public SearchResult search(SearchRequest request) {
        // 调用 RAGFlow 检索 API
        // POST /api/v1/datasets/{dataset_id}/chunks/search
    }

    // ...
}
```

| 外部知识库 | 适配类 | 说明 |
|-----------|--------|------|
| RAGFlow | RagFlowKnowledgeRepository | 通过 RAGFlow REST API 封装，对接其文档管理和检索能力 |
| Dify | DifyKnowledgeRepository（二期） | 预留扩展 |
| 自定义 | 实现 KnowledgeRepository 接口 + 注册 Bean | 插件化接入 |

RAGFlow 适配器通过配置激活：
```yaml
knowledge:
  repository:
    type: ragflow              # milvus（内置）/ ragflow（外部）
    ragflow:
      endpoint: http://ragflow:9380
      api-key: ${RAGFLOW_API_KEY}
```

**网络搜索 — 搜索引擎适配器**：

```java
public interface SearchEngineAdapter {
    String getEngineName();
    SearchEngineType getType();  // WEB / NEWS / ACADEMIC / CODE
    List<SearchResult> search(SearchQuery query);
    // 可选：将搜索结果持久化到知识库
    boolean supportPersistence();
}
```

| 适配器 | 说明 |
|--------|------|
| dashscope-search | 阿里云 DashScope 搜索（默认） |
| tavily-search | Tavily Search API（AI优化搜索） |
| custom-search | 自定义搜索引擎对接接口 |

**网络搜索结果处理管线**：搜索查询 → 调用搜索引擎 → 结果清洗去重 → 向量化 → 可选存入Milvus

连接器和搜索适配器均支持插件化注册，第三方可开发自定义实现。

**数据存储**：

- PG: kb_config / kb_document / kb_chunk / kb_index / connector_config
- Milvus: 向量索引 + 向量检索

---

### 2. workflow-service（工作流编排服务）

**职责**：Graph工作流定义/执行/监控 + Human-in-the-Loop

**DAG节点类型**：

| 节点类型 | 说明 | 调用目标 |
|---------|------|---------|
| AGENT | 调用Agent执行推理 | agent-service |
| SKILL | 调用已注册的Skill | skill-service |
| TOOL | 调用MCP工具 | mcp-gateway |
| KNOWLEDGE | 检索知识库 | knowledge-service |
| CONDITION | 条件路由分支 | 内置表达式引擎 |
| PARALLEL | 并行扇出/汇聚 | 内置线程池 |
| LLM_CALL | 直接LLM调用 | model-gateway |
| CODE | 沙箱执行脚本 | GraalJS |
| WAIT | 等待外部事件/人工审批 | RocketMQ回调 |

**工作流定义方式**：JSON DSL文件导入、API编程式构建、平台可视化拖拽编辑器

**HITL（Human-in-the-Loop）**：
- 审批任务队列：待审批列表，支持通过/驳回/转派
- SLA追踪：审批超时告警、自动升级
- 干预接口：Agent执行中人工注入指令修正方向
- 标注沉淀：审批决策自动进入评估数据集

**数据存储**：

- PG: wf_definition / wf_instance / wf_node_exec / wf_template / approval_task
- Redis: 运行态上下文缓存 / 分布式锁
- RocketMQ: 异步节点执行 / 事件通知

---

### 3. agent-service（Agent服务）

**职责**：Agent生命周期管理 + 多模式协调 + 增强会话管理

**三种协调模式**：

| 模式 | 场景 | 实现 |
|------|------|------|
| ReAct Loop | 单Agent推理+工具调用 | 思考→行动→观察循环，ToolRegistry自动发现工具 |
| Graph Bridge | 确定性多步骤编排 | 向workflow-service提交工作流实例 |
| Negotiation | 开放协作/决策 | Agent间RocketMQ消息交换（辩论/投票/仲裁） |

**Agent定义模型**：

```json
{
  "id": "agent_001",
  "name": "技术支持Agent",
  "systemPrompt": "...",
  "model": "qwen-plus",
  "tools": ["kb_search", "ticket_query", "doc_gen"],
  "skills": ["troubleshoot_v1", "escalation_v2"],
  "knowledgeBases": ["kb_tech_docs", "kb_faq"],
  "coordinationMode": "react|graph|negotiation",
  "constraints": { "maxSteps": 20, "temperature": 0.3 }
}
```

**会话管理增强**：
- 对话树分支：支持从任意节点分叉
- 上下文窗口管理：自动摘要/裁剪历史，防止超出Token限制
- 会话持久化：跨设备/跨时间恢复
- 多模态支持：消息中携带图片/文件/结构化数据

**数据存储**：

- PG: agent_def / agent_session / agent_message / agent_negotiation
- Redis: 会话上下文 / ReAct状态 / 协商临时数据
- RocketMQ: Agent间协商消息 / 异步执行事件

---

### 4. skill-service（Skill服务）

**职责**：三层Skill注册与执行 + 发现匹配

**三层体系**：

| 层级 | 类型 | 说明 |
|------|------|------|
| Layer 1 | PromptSkill | 提示词模板 + 参数占位 + 版本管理 |
| Layer 2 | FunctionSkill | JSON Schema约束 + 同步/异步执行 + 沙箱安全 |
| Layer 3 | AgentSkill | 完整子Agent定义 + 上下文隔离 + 异步委派 |

**Skill发现**：语义匹配（向量检索）、标签匹配（精确筛选）、自动组合（多Skill协作）

**关键设计**：Skill热加载（Redis缓存+变动通知）、语义版本控制（多版本共存）、沙箱安全执行（GraalJS，CPU/内存/网络限制）

**数据存储**：

- PG: skill_def / skill_version / skill_execution_log
- Redis: Skill热缓存
- RocketMQ: 子Agent异步执行 / 结果回调

---

### 5. mcp-gateway（MCP网关服务）

**职责**：MCP Client（调用外部）+ MCP Server（对外暴露）

**Client侧**：连接池管理、SSE/Streamable HTTP双协议、健康检查+断路器、工具代理层、结果缓存、审计日志

**Server侧**：
- 导出Agent工具、知识库检索、Skill模板、工作流运行
- SSE Transport + Streamable HTTP Transport
- API Key认证保护

**数据存储**：

- PG: mcp_server_reg / mcp_client_conn / mcp_tool_reg / mcp_audit
- Redis: 连接状态 / 工具缓存 / 会话Token

---

### 6. model-gateway（模型网关）

**职责**：多模型统一接入与路由

**核心能力**：
- 多模型适配：DashScope / OpenAI / 本地模型统一接口
- 智能路由：按任务复杂度分流（简单意图→轻量模型，复杂推理→强模型）
- Fallback链路：主模型不可用时自动降级
- 成本追踪：按模型/Agent维度统计Token消耗

**数据存储**：

- PG: model_config / model_route_rule / model_call_log
- Redis: 模型状态 / 路由缓存

---

### 7. eval-service（评估服务）

**职责**：评估与回归测试

**核心能力**：
- RAG评估：MRR/NDCG/答案忠实度
- Agent评估：端到端任务成功率、工具调用准确率、步数效率
- 回归测试集管理：积累BadCase形成测试集
- 人工标注接入：人工打分，持续积累

**数据存储**：

- PG: eval_dataset / eval_task / eval_result / annotation_record
- Redis: 评估任务状态

---

### 8. platform-service（管理后台）

**职责**：Web管理控制台 + Prompt管理中心 + 审批工作台

**子模块**：

| 模块 | 能力 |
|------|------|
| 配置管理 | 模型、Agent、知识库、MCP连接的统一配置 |
| Skill市场 | Skill浏览/安装/启停/评分 |
| Prompt管理中心 | 版本控制/变量注入/合规过滤 |
| 审批工作台 | 待审批任务/SLA追踪/决策标注沉淀 |
| 工作流编辑器 | React Flow拖拽式DAG可视化编辑 |

---

### 9. 公共模块与基础设施

**ai-base-common**：
- 统一DTO / 异常处理 / 工具类 / BaseEntity / SnowflakeIdGenerator
- AI安全过滤器：Prompt注入检测、越狱防护、敏感数据脱敏（手机号/身份证/密钥）

**ai-base-api**：Feign接口定义，所有服务间契约

**可观测性**：
- 协议：OpenTelemetry（OTLP导出），Micrometer桥接，Spring Boot 3原生自动装配
- 链路追踪：Grafana Tempo 存储，全链路TraceID贯穿 Gateway → agent → workflow → skill → knowledge → LLM
- 日志采集：Loki + Promtail，结构化JSON日志，TraceID注入每条日志
- 统一看板：Grafana（Tempo数据源 + Loki数据源）
- LLM调用观测：Token消耗/延迟/模型/缓存命中，通过Span属性上报
- Agent执行回放：完整记录Think-Act-Observe每步，Trace Span级联

**日志采集配置**：

```yaml
# application.yml - 各服务公共配置
observability:
  logging:
    enabled: true              # 日志采集开关
    level: INFO
    format: json               # 结构化JSON，注入TraceID/SpanID
  tracing:
    enabled: true              # 链路追踪开关
    endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
    sampler: parentbased_traceidratio  # 采样策略：跟随父级决策
    sample-rate: 1.0           # 全量采样（开发/测试）, 生产建议0.1-0.3
```

---

## 数据库设计

### 通用字段规范

所有业务表统一包含以下字段：

```sql
id          VARCHAR(32)  PRIMARY KEY,     -- 雪花算法生成
created_at  TIMESTAMP    DEFAULT NOW(),   -- 创建时间
updated_at  TIMESTAMP    DEFAULT NOW(),   -- 更新时间
created_by  VARCHAR(32),                  -- 创建人
updated_by  VARCHAR(32)                   -- 更新人
```

---

### 1. knowledge-service

#### 表结构 DDL

```sql
-- 知识库配置
CREATE TABLE kb_config (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,        -- 知识库名称
    description TEXT,                         -- 描述
    kb_type     VARCHAR(16)  NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC/PERSONAL/DEPARTMENT
    owner_id    VARCHAR(32),                  -- 个人知识库所有者UserId
    owner_dept_id VARCHAR(32),                -- 部门知识库所属部门DeptId
    embedding_model VARCHAR(128) NOT NULL,    -- Embedding模型
    chunk_size  INTEGER DEFAULT 800,          -- 切分块大小
    chunk_overlap INTEGER DEFAULT 100,        -- 切分重叠
    status      VARCHAR(16) DEFAULT 'ACTIVE', -- ACTIVE/DISABLED/DELETED
    metadata    JSONB,                        -- 扩展配置
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 文档记录
CREATE TABLE kb_document (
    id          VARCHAR(32)  PRIMARY KEY,
    kb_id       VARCHAR(32)  NOT NULL REFERENCES kb_config(id),
    title       VARCHAR(256) NOT NULL,        -- 文档标题
    source_type VARCHAR(32)  NOT NULL,        -- UPLOAD/CONNECTOR/SEARCH/API
    source_ref  VARCHAR(512),                 -- 来源引用
    file_type   VARCHAR(16),                  -- PDF/DOCX/MD/TXT/HTML
    file_size   BIGINT,                       -- 字节数
    status      VARCHAR(16) DEFAULT 'PENDING',-- PENDING/SPLITTING/EMBEDDING/READY/FAILED
    chunk_count INTEGER DEFAULT 0,            -- 切分块数
    checksum    VARCHAR(64),                  -- 文件Hash，用于增量去重
    ingested_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 文档分块
CREATE TABLE kb_chunk (
    id          VARCHAR(32)  PRIMARY KEY,
    doc_id      VARCHAR(32)  NOT NULL REFERENCES kb_document(id),
    kb_id       VARCHAR(32)  NOT NULL REFERENCES kb_config(id),
    chunk_index INTEGER      NOT NULL,        -- 块序号
    content     TEXT         NOT NULL,        -- 文本内容
    token_count INTEGER,                      -- Token数
    vector_id   VARCHAR(64),                  -- Milvus中的向量ID
    metadata    JSONB,                        -- 页码/章节/标题等
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 数据源连接器配置
CREATE TABLE connector_config (
    id          VARCHAR(32)  PRIMARY KEY,
    kb_id       VARCHAR(32)  NOT NULL REFERENCES kb_config(id),
    connector_type VARCHAR(32) NOT NULL,      -- FS/DB/UPLOAD/API
    name        VARCHAR(128) NOT NULL,
    config      JSONB        NOT NULL,        -- 连接器特定配置
    sync_cron   VARCHAR(32),                  -- 定时同步Cron
    last_sync_at TIMESTAMP,                   -- 上次同步时间
    last_sync_token VARCHAR(256),             -- 增量同步Token
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 搜索引擎配置
CREATE TABLE search_engine_config (
    id          VARCHAR(32)  PRIMARY KEY,
    kb_id       VARCHAR(32)  NOT NULL REFERENCES kb_config(id),
    engine_type VARCHAR(32)  NOT NULL,        -- DASHSCOPE/TAVILY/CUSTOM
    name        VARCHAR(128) NOT NULL,
    config      JSONB        NOT NULL,        -- API Key等
    persist_results BOOLEAN DEFAULT FALSE,    -- 是否持久化搜索结果
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// KbConfig.java - MyBatis POJO，驼峰自动映射下划线列名
public class KbConfig extends BaseEntity {
    private String name;
    private String description;
    private String kbType = "PUBLIC";       // PUBLIC / PERSONAL / DEPARTMENT
    private String ownerId;                 // 个人知识库所有者
    private String ownerDeptId;             // 部门知识库所属部门
    private String embeddingModel;
    private Integer chunkSize = 800;
    private Integer chunkOverlap = 100;
    private String status = "ACTIVE";
    private String metadata;                // JSONB → String，PG驱动自动转换
    // getters/setters ...
}

// KbDocument.java — MyBatis POJO，表名 kb_document（驼峰自动转下划线映射）
public class KbDocument extends BaseEntity {
    private String kbId;
    private String title;
    private SourceType sourceType;  // 枚举，MyBatis EnumTypeHandler 自动映射
    private String sourceRef;
    private String fileType;
    private Long fileSize;
    private IngestStatus status = IngestStatus.PENDING;
    private Integer chunkCount = 0;
    private String checksum;
    private LocalDateTime ingestedAt;
    // getters/setters ...
}

// KbChunk.java — MyBatis POJO，表名 kb_chunk
public class KbChunk extends BaseEntity {
    private String docId;
    private String kbId;
    private Integer chunkIndex;
    private String content;         // TEXT 列 → String，PG驱动自动转换
    private Integer tokenCount;
    private String vectorId;
    private String metadata;        // JSONB → String，PG驱动自动转换
    // getters/setters ...
}

// ConnectorConfig.java — MyBatis POJO，表名 connector_config
public class ConnectorConfig extends BaseEntity {
    private String kbId;
    private String connectorType;
    private String name;
    private String config;          // JSONB → String
    private String syncCron;
    private LocalDateTime lastSyncAt;
    private String lastSyncToken;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// SearchEngineConfig.java — MyBatis POJO，表名 search_engine_config
public class SearchEngineConfig extends BaseEntity {
    private String kbId;
    private String engineType;
    private String name;
    private String config;          // JSONB → String
    private Boolean persistResults = false;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}
```

---

### 2. workflow-service

#### 表结构 DDL

```sql
-- 工作流定义
CREATE TABLE wf_definition (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    version     INTEGER DEFAULT 1,
    dag         JSONB        NOT NULL,        -- {"nodes":[],"edges":[]}
    timeout_seconds INTEGER DEFAULT 300,
    retry_policy JSONB,                       -- {"maxRetries":3,"backoff":"exponential"}
    status      VARCHAR(16) DEFAULT 'DRAFT',  -- DRAFT/PUBLISHED/DEPRECATED
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 工作流实例
CREATE TABLE wf_instance (
    id          VARCHAR(32)  PRIMARY KEY,
    definition_id VARCHAR(32) NOT NULL REFERENCES wf_definition(id),
    definition_version INTEGER,
    status      VARCHAR(16) DEFAULT 'RUNNING',-- RUNNING/PAUSED/COMPLETED/FAILED/CANCELLED
    input       JSONB,
    output      JSONB,
    context     JSONB,                        -- 运行时上下文
    started_at  TIMESTAMP,
    completed_at TIMESTAMP,
    error_msg   TEXT,
    trace_id    VARCHAR(64),                  -- OpenTelemetry TraceID
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 节点执行记录
CREATE TABLE wf_node_exec (
    id          VARCHAR(32)  PRIMARY KEY,
    instance_id VARCHAR(32)  NOT NULL REFERENCES wf_instance(id),
    node_id     VARCHAR(64)  NOT NULL,        -- DAG中节点ID
    node_type   VARCHAR(16)  NOT NULL,        -- AGENT/SKILL/TOOL/KNOWLEDGE/CONDITION/PARALLEL/LLM_CALL/CODE/WAIT
    status      VARCHAR(16) DEFAULT 'PENDING',-- PENDING/RUNNING/COMPLETED/FAILED/SKIPPED
    input       JSONB,
    output      JSONB,
    started_at  TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms INTEGER,
    retry_count  INTEGER DEFAULT 0,
    error_msg   TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 工作流模板
CREATE TABLE wf_template (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    category    VARCHAR(32),                  -- RAG/MULTI_STEP/REVIEW/ETL
    dag         JSONB        NOT NULL,
    usage_count INTEGER DEFAULT 0,
    status      VARCHAR(16) DEFAULT 'PUBLISHED',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 审批任务
CREATE TABLE approval_task (
    id          VARCHAR(32)  PRIMARY KEY,
    instance_id VARCHAR(32)  NOT NULL REFERENCES wf_instance(id),
    node_exec_id VARCHAR(32) NOT NULL REFERENCES wf_node_exec(id),
    title       VARCHAR(256) NOT NULL,
    detail      TEXT,
    assignee_id VARCHAR(32),                  -- 指定审批人
    status      VARCHAR(16) DEFAULT 'PENDING',-- PENDING/APPROVED/REJECTED/TRANSFERRED
    sla_deadline TIMESTAMP,                   -- SLA截止时间
    sla_escalated BOOLEAN DEFAULT FALSE,      -- 是否已升级
    decided_by  VARCHAR(32),                  -- 决策人
    decision    VARCHAR(16),                  -- APPROVE/REJECT
    comment     TEXT,                         -- 审批意见
    decided_at  TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// WfDefinition.java
public class WfDefinition extends BaseEntity {
    private String name;
    private String description;
    private Integer version = 1;
    private String dag;  // JSONB → String: {"nodes":[],"edges":[]}
    private Integer timeoutSeconds = 300;
    private String retryPolicy;      // JSONB → String
    private DefinitionStatus status = DefinitionStatus.DRAFT;
    // getters/setters ...
}

// WfInstance.java — MyBatis POJO，表名 wf_instance
public class WfInstance extends BaseEntity {
    private String definitionId;
    private Integer definitionVersion;
    private InstanceStatus status = InstanceStatus.RUNNING;
    private String input;            // JSONB → String
    private String output;           // JSONB → String
    private String context;          // JSONB → String
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMsg;
    private String traceId;
    // getters/setters ...
}

// WfNodeExec.java — MyBatis POJO，表名 wf_node_exec
public class WfNodeExec extends BaseEntity {
    private String instanceId;
    private String nodeId;
    private NodeType nodeType;
    private ExecStatus status = ExecStatus.PENDING;
    private String input;            // JSONB → String
    private String output;           // JSONB → String
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationMs;
    private Integer retryCount = 0;
    private String errorMsg;
    // getters/setters ...
}

// ApprovalTask.java — MyBatis POJO，表名 approval_task
public class ApprovalTask extends BaseEntity {
    private String instanceId;
    private String nodeExecId;
    private String title;
    private String detail;
    private String assigneeId;
    private ApprovalStatus status = ApprovalStatus.PENDING;
    private LocalDateTime slaDeadline;
    private Boolean slaEscalated = false;
    private String decidedBy;
    private String decision;
    private String comment;
    private LocalDateTime decidedAt;
    // getters/setters ...
}
```

---

### 3. agent-service

#### 表结构 DDL

```sql
-- Agent定义
CREATE TABLE agent_def (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    system_prompt TEXT       NOT NULL,        -- 系统提示词
    model       VARCHAR(64)  NOT NULL,        -- 模型名称
    tools       JSONB,                        -- ["kb_search","ticket_query"]
    skill_ids   JSONB,                        -- ["skill_001","skill_002"]
    kb_ids      JSONB,                        -- ["kb_tech_docs","kb_faq"]
    coordination_mode VARCHAR(16) NOT NULL,   -- REACT/GRAPH/NEGOTIATION
    constraints JSONB,                        -- {"maxSteps":20,"temperature":0.3}
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Agent会话
CREATE TABLE agent_session (
    id          VARCHAR(32)  PRIMARY KEY,
    agent_id    VARCHAR(32)  NOT NULL REFERENCES agent_def(id),
    user_id     VARCHAR(32),
    title       VARCHAR(256),                 -- 会话标题
    status      VARCHAR(16) DEFAULT 'ACTIVE', -- ACTIVE/COMPLETED/ERROR
    context     JSONB,                        -- 当前上下文状态
    trace_id    VARCHAR(64),
    started_at  TIMESTAMP,
    completed_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Agent消息
CREATE TABLE agent_message (
    id          VARCHAR(32)  PRIMARY KEY,
    session_id  VARCHAR(32)  NOT NULL REFERENCES agent_session(id),
    parent_id   VARCHAR(32),                  -- 对话树父节点，NULL为根
    role        VARCHAR(16)  NOT NULL,        -- USER/ASSISTANT/SYSTEM/TOOL
    content     TEXT,
    content_type VARCHAR(16) DEFAULT 'TEXT',  -- TEXT/IMAGE/FILE/STRUCTURED
    attachments JSONB,                        -- 多模态附件
    tool_calls  JSONB,                        -- 工具调用记录
    token_count INTEGER,
    metadata    JSONB,                        -- 模型/延迟/成本等
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Agent间协商记录
CREATE TABLE agent_negotiation (
    id          VARCHAR(32)  PRIMARY KEY,
    session_id VARCHAR(32)   NOT NULL,        -- 协商会话
    round       INTEGER      NOT NULL,        -- 轮次
    from_agent_id VARCHAR(32) NOT NULL,
    msg_type    VARCHAR(16)  NOT NULL,        -- PROPOSE/AGREE/REFUTE/MODIFY/ARBITRATE
    content     TEXT         NOT NULL,
    confidence  DECIMAL(3,2),                 -- 置信度 0.00-1.00
    reasoning   TEXT,                         -- 推理过程
    metadata    JSONB,
    created_at  TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// AgentDef.java — MyBatis POJO，表名 agent_def
public class AgentDef extends BaseEntity {
    private String name;
    private String description;
    private String systemPrompt;     // TEXT → String
    private String model;
    private String tools;            // JSONB → String: ["kb_search","ticket_query"]
    private String skillIds;         // JSONB → String: ["skill_001","skill_002"]
    private String kbIds;            // JSONB → String: ["kb_tech_docs","kb_faq"]
    private CoordinationMode coordinationMode;  // 枚举，MyBatis EnumTypeHandler 自动映射
    private String constraints;      // JSONB → String: {"maxSteps":20,"temperature":0.3}
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// AgentSession.java — MyBatis POJO，表名 agent_session
public class AgentSession extends BaseEntity {
    private String agentId;
    private String userId;
    private String title;
    private SessionStatus status = SessionStatus.ACTIVE;
    private String context;          // JSONB → String
    private String traceId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    // getters/setters ...
}

// AgentMessage.java — MyBatis POJO，表名 agent_message
public class AgentMessage extends BaseEntity {
    private String sessionId;
    private String parentId;  // 对话树分支，NULL为根
    private MessageRole role;
    private String content;
    private ContentType contentType = ContentType.TEXT;
    private String attachments;      // JSONB → String (多模态附件)
    private String toolCalls;        // JSONB → String (工具调用记录)
    private Integer tokenCount;
    private String metadata;         // JSONB → String (模型/延迟/成本等)
    // getters/setters ...
}

// AgentNegotiation.java — MyBatis POJO，表名 agent_negotiation
public class AgentNegotiation extends BaseEntity {
    private String sessionId;
    private Integer round;
    private String fromAgentId;
    private NegotiationMsgType msgType;
    private String content;
    private BigDecimal confidence;
    private String reasoning;
    private String metadata;         // JSONB → String
    // getters/setters ...
}
```

---

### 4. skill-service

#### 表结构 DDL

```sql
-- Skill定义
CREATE TABLE skill_def (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,                         -- 用于语义匹配
    tags        JSONB,                        -- ["客服","技术支持","故障排查"]
    skill_level VARCHAR(16)  NOT NULL,        -- PROMPT/FUNCTION/AGENT
    -- Layer 1: PROMPT
    prompt_template TEXT,                     -- 提示词模板
    params       JSONB,                       -- 参数定义
    -- Layer 2: FUNCTION
    input_schema  JSONB,                      -- 输入JSON Schema
    output_schema JSONB,                      -- 输出JSON Schema
    execution_mode VARCHAR(8),                -- SYNC/ASYNC
    timeout_ms     INTEGER,
    -- Layer 3: AGENT
    agent_ref_id VARCHAR(32),                 -- 关联agent_def.id
    -- 通用
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Skill版本
CREATE TABLE skill_version (
    id          VARCHAR(32)  PRIMARY KEY,
    skill_id    VARCHAR(32)  NOT NULL REFERENCES skill_def(id),
    version     VARCHAR(16)  NOT NULL,        -- 语义版本 "1.2.0"
    changelog   TEXT,
    definition  JSONB        NOT NULL,        -- 该版本的完整定义快照
    is_latest   BOOLEAN DEFAULT FALSE,
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Skill执行日志
CREATE TABLE skill_execution_log (
    id          VARCHAR(32)  PRIMARY KEY,
    skill_id    VARCHAR(32)  NOT NULL,
    skill_version VARCHAR(16),
    session_id  VARCHAR(32),                  -- 调用方会话
    input       JSONB,
    output      JSONB,
    status      VARCHAR(16) DEFAULT 'RUNNING',-- RUNNING/SUCCESS/FAILED/TIMEOUT
    duration_ms INTEGER,
    error_msg   TEXT,
    trace_id    VARCHAR(64),
    created_at  TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// SkillDef.java — MyBatis POJO，表名 skill_def
public class SkillDef extends BaseEntity {
    private String name;
    private String description;
    private String tags;             // JSONB → String: ["客服","技术支持"]
    private SkillLevel skillLevel;   // PROMPT/FUNCTION/AGENT
    // Layer 1: PROMPT
    private String promptTemplate;   // TEXT → String
    private String params;           // JSONB → String (参数定义)
    // Layer 2: FUNCTION
    private String inputSchema;      // JSONB → String (输入JSON Schema)
    private String outputSchema;     // JSONB → String (输出JSON Schema)
    private ExecutionMode executionMode;  // SYNC/ASYNC
    private Integer timeoutMs;
    // Layer 3: AGENT
    private String agentRefId;       // 关联 agent_def.id
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// SkillVersion.java — MyBatis POJO，表名 skill_version
public class SkillVersion extends BaseEntity {
    private String skillId;
    private String version;          // 语义版本 "1.2.0"
    private String changelog;
    private String definition;       // JSONB → String (版本完整快照)
    private Boolean isLatest = false;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// SkillExecutionLog.java — MyBatis POJO，表名 skill_execution_log
// 继承 BaseEntity，ID 和时间戳由 SnowflakeAutoFillInterceptor 自动填充
public class SkillExecutionLog extends BaseEntity {
    private String skillId;
    private String skillVersion;
    private String sessionId;
    private String input;            // JSONB → String
    private String output;           // JSONB → String
    private ExecStatus status;
    private Integer durationMs;
    private String errorMsg;
    private String traceId;
    // getters/setters ...
}
```

---

### 5. mcp-gateway

#### 表结构 DDL

```sql
-- MCP Server注册
CREATE TABLE mcp_server_reg (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    server_type VARCHAR(16)  NOT NULL,        -- BUILTIN/EXTERNAL
    transport   VARCHAR(16)  NOT NULL,        -- SSE/STREAMABLE_HTTP
    endpoint    VARCHAR(512) NOT NULL,        -- 连接端点
    auth_config JSONB,                        -- {"type":"API_KEY","key":"***"}
    tools_count INTEGER DEFAULT 0,
    resources_count INTEGER DEFAULT 0,
    prompts_count INTEGER DEFAULT 0,
    health_status VARCHAR(16) DEFAULT 'UNKNOWN', -- HEALTHY/UNHEALTHY/DEGRADED
    last_health_check TIMESTAMP,
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- MCP Client连接
CREATE TABLE mcp_client_conn (
    id          VARCHAR(32)  PRIMARY KEY,
    server_id   VARCHAR(32)  NOT NULL REFERENCES mcp_server_reg(id),
    status      VARCHAR(16) DEFAULT 'DISCONNECTED', -- CONNECTED/DISCONNECTED/RECONNECTING
    session_token VARCHAR(256),
    connected_at  TIMESTAMP,
    disconnected_at TIMESTAMP,
    error_count   INTEGER DEFAULT 0,
    last_error    TEXT,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- MCP工具注册
CREATE TABLE mcp_tool_reg (
    id          VARCHAR(32)  PRIMARY KEY,
    server_id   VARCHAR(32)  NOT NULL REFERENCES mcp_server_reg(id),
    tool_name   VARCHAR(128) NOT NULL,
    description TEXT,
    input_schema  JSONB      NOT NULL,        -- 工具输入Schema
    tool_type   VARCHAR(16)  NOT NULL,        -- EXTERNAL/BUILTIN (内置导出)
    source_service VARCHAR(32),               -- 内置工具的来源服务
    cache_ttl_seconds INTEGER,                -- 结果缓存TTL
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- MCP审计日志
CREATE TABLE mcp_audit (
    id          VARCHAR(32)  PRIMARY KEY,
    server_id   VARCHAR(32)  NOT NULL,
    tool_name   VARCHAR(128) NOT NULL,
    session_id VARCHAR(32),
    caller      VARCHAR(64),                  -- 调用方标识
    input       JSONB,
    output      JSONB,
    status      VARCHAR(16),                  -- SUCCESS/FAILED/TIMEOUT
    duration_ms INTEGER,
    error_msg   TEXT,
    trace_id    VARCHAR(64),
    created_at  TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// McpServerReg.java — MyBatis POJO，表名 mcp_server_reg
public class McpServerReg extends BaseEntity {
    private String name;
    private ServerType serverType;   // BUILTIN/EXTERNAL
    private TransportType transport; // SSE/STREAMABLE_HTTP
    private String endpoint;
    private String authConfig;       // JSONB → String: {"type":"API_KEY","key":"***"}
    private Integer toolsCount = 0;
    private Integer resourcesCount = 0;
    private Integer promptsCount = 0;
    private HealthStatus healthStatus = HealthStatus.UNKNOWN;
    private LocalDateTime lastHealthCheck;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// McpClientConn.java — MyBatis POJO，表名 mcp_client_conn
public class McpClientConn extends BaseEntity {
    private String serverId;
    private ConnStatus status = ConnStatus.DISCONNECTED;
    private String sessionToken;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
    private Integer errorCount = 0;
    private String lastError;        // TEXT → String
    // getters/setters ...
}

// McpToolReg.java — MyBatis POJO，表名 mcp_tool_reg
public class McpToolReg extends BaseEntity {
    private String serverId;
    private String toolName;
    private String description;
    private String inputSchema;      // JSONB → String (输入JSON Schema)
    private ToolType toolType;
    private String sourceService;
    private Integer cacheTtlSeconds;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}
```

---

### 6. model-gateway

#### 表结构 DDL

```sql
-- 模型配置
CREATE TABLE model_config (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,        -- qwen-plus/gpt-4/deepseek-v3
    provider    VARCHAR(32)  NOT NULL,        -- DASHSCOPE/OPENAI/LOCAL
    endpoint    VARCHAR(512) NOT NULL,
    api_key_ref VARCHAR(128),                 -- 凭据引用，不存明文
    max_tokens  INTEGER,
    default_temperature DECIMAL(2,1) DEFAULT 0.7,
    capabilities JSONB,                       -- ["chat","embedding","vision","function_calling"]
    priority    INTEGER DEFAULT 0,            -- 同能力模型优先级
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 路由规则
CREATE TABLE model_route_rule (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    condition   JSONB        NOT NULL,        -- {"taskComplexity":"LOW","maxTokens":4096}
    target_model_id VARCHAR(32) NOT NULL REFERENCES model_config(id),
    fallback_model_id VARCHAR(32) REFERENCES model_config(id), -- 降级模型
    priority    INTEGER DEFAULT 0,
    status      VARCHAR(16) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 模型调用日志
CREATE TABLE model_call_log (
    id          VARCHAR(32)  PRIMARY KEY,
    model_id    VARCHAR(32)  NOT NULL,
    model_name  VARCHAR(128) NOT NULL,
    caller      VARCHAR(64),                  -- agent_id/skill_id/workflow_instance_id
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    total_tokens  INTEGER,
    latency_ms    INTEGER,
    cached        BOOLEAN DEFAULT FALSE,      -- 是否命中缓存
    fallback_used BOOLEAN DEFAULT FALSE,      -- 是否使用降级模型
    cost_cents    DECIMAL(10,4),              -- 花费（美分）
    status        VARCHAR(16),                -- SUCCESS/FAILED/TIMEOUT
    error_msg     TEXT,
    trace_id      VARCHAR(64),
    created_at    TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// ModelConfig.java — MyBatis POJO，表名 model_config
public class ModelConfig extends BaseEntity {
    private String name;             // qwen-plus/gpt-4/deepseek-v3
    private ModelProvider provider;  // DASHSCOPE/OPENAI/LOCAL
    private String endpoint;
    private String apiKeyRef;        // 凭据引用，不存明文
    private Integer maxTokens;
    private BigDecimal defaultTemperature = new BigDecimal("0.7");
    private String capabilities;     // JSONB → String: ["chat","embedding","vision"]
    private Integer priority = 0;    // 同能力模型优先级
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// ModelRouteRule.java — MyBatis POJO，表名 model_route_rule
public class ModelRouteRule extends BaseEntity {
    private String name;
    private String condition;        // JSONB → String: {"taskComplexity":"LOW","maxTokens":4096}
    private String targetModelId;
    private String fallbackModelId;  // 降级模型
    private Integer priority = 0;
    private ConfigStatus status = ConfigStatus.ACTIVE;
    // getters/setters ...
}

// ModelCallLog.java — MyBatis POJO，表名 model_call_log
// 继承 BaseEntity，ID 和时间戳由 SnowflakeAutoFillInterceptor 自动填充
public class ModelCallLog extends BaseEntity {
    private String modelId;
    private String modelName;
    private String caller;           // agent_id/skill_id/workflow_instance_id
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer latencyMs;
    private Boolean cached = false;
    private Boolean fallbackUsed = false;
    private BigDecimal costCents;
    private String status;           // SUCCESS/FAILED/TIMEOUT
    private String errorMsg;
    private String traceId;
    // getters/setters ...
}
```

---

### 7. eval-service

#### 表结构 DDL

```sql
-- 评估数据集
CREATE TABLE eval_dataset (
    id          VARCHAR(32)  PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    eval_type   VARCHAR(16)  NOT NULL,        -- RAG/AGENT/SKILL/END_TO_END
    item_count  INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 评估数据项
CREATE TABLE eval_dataset_item (
    id          VARCHAR(32)  PRIMARY KEY,
    dataset_id  VARCHAR(32)  NOT NULL REFERENCES eval_dataset(id),
    question    TEXT         NOT NULL,
    expected_answer TEXT,                     -- 期望答案
    context     JSONB,                        -- 参考答案/上下文
    metadata    JSONB,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 评估任务
CREATE TABLE eval_task (
    id          VARCHAR(32)  PRIMARY KEY,
    dataset_id  VARCHAR(32)  NOT NULL REFERENCES eval_dataset(id),
    target_id   VARCHAR(32),                  -- 被评估的Agent/Skill/知识库ID
    target_type VARCHAR(16),                  -- AGENT/SKILL/KNOWLEDGE/WORKFLOW
    status      VARCHAR(16) DEFAULT 'PENDING',-- PENDING/RUNNING/COMPLETED/FAILED
    metrics     JSONB,                        -- {"mrr":0.85,"ndcg":0.92,"accuracy":0.88}
    total_items INTEGER,
    passed_items INTEGER,
    started_at  TIMESTAMP,
    completed_at TIMESTAMP,
    trace_id    VARCHAR(64),
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 评估结果明细
CREATE TABLE eval_result (
    id          VARCHAR(32)  PRIMARY KEY,
    task_id     VARCHAR(32)  NOT NULL REFERENCES eval_task(id),
    item_id     VARCHAR(32)  NOT NULL REFERENCES eval_dataset_item(id),
    actual_output TEXT,                       -- 实际输出
    metrics     JSONB,                        -- 单项指标
    passed      BOOLEAN,
    error_msg   TEXT,
    duration_ms INTEGER,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- 人工标注
CREATE TABLE annotation_record (
    id          VARCHAR(32)  PRIMARY KEY,
    eval_result_id VARCHAR(32) NOT NULL REFERENCES eval_result(id),
    annotator_id VARCHAR(32),
    score       INTEGER,                      -- 1-5分
    tags        JSONB,                        -- ["准确","简洁"]
    comment     TEXT,
    is_golden   BOOLEAN DEFAULT FALSE,        -- 是否标注为黄金标准
    created_at  TIMESTAMP DEFAULT NOW()
);
```

#### Java 实体类

```java
// EvalDataset.java — MyBatis POJO，表名 eval_dataset
public class EvalDataset extends BaseEntity {
    private String name;
    private String description;
    private EvalType evalType;
    private Integer itemCount = 0;
    // getters/setters ...
}

// EvalTask.java — MyBatis POJO，表名 eval_task
public class EvalTask extends BaseEntity {
    private String datasetId;
    private String targetId;
    private TargetType targetType;
    private TaskStatus status = TaskStatus.PENDING;
    private String metrics;          // JSONB → String
    private Integer totalItems;
    private Integer passedItems;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String traceId;
    // getters/setters ...
}
```

---

### 8. platform-service

#### 表结构 DDL

```sql
-- Prompt版本
CREATE TABLE prompt_version (
    id          VARCHAR(32)  PRIMARY KEY,
    ref_type    VARCHAR(32)  NOT NULL,        -- AGENT/SKILL/WORKFLOW
    ref_id      VARCHAR(32)  NOT NULL,        -- 关联对象ID
    version     INTEGER      NOT NULL,
    content     TEXT         NOT NULL,
    changelog   TEXT,
    is_current  BOOLEAN DEFAULT FALSE,
    status      VARCHAR(16) DEFAULT 'DRAFT',  -- DRAFT/PUBLISHED/ROLLED_BACK
    created_at  TIMESTAMP DEFAULT NOW(),
    created_by  VARCHAR(32)
);
```

#### Java 实体类

```java
// PromptVersion.java — MyBatis POJO，表名 prompt_version
// 继承 BaseEntity，ID 和时间戳由 SnowflakeAutoFillInterceptor 自动填充
public class PromptVersion extends BaseEntity {
    private String refType;          // AGENT/SKILL/WORKFLOW
    private String refId;            // 关联对象ID
    private Integer version;
    private String content;          // TEXT → String
    private String changelog;
    private Boolean isCurrent = false;
    private PromptStatus status = PromptStatus.DRAFT;
    // getters/setters ...
}
```

---

### 公共基类

```java
// BaseEntity.java - MyBatis POJO 基类，通过拦截器自动填充 ID + 时间戳
public abstract class BaseEntity {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    // getters/setters ...
}

// SnowflakeAutoFillInterceptor.java - MyBatis 拦截器，替代 JPA @PrePersist/@PreUpdate
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class SnowflakeAutoFillInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        if (parameter instanceof BaseEntity entity) {
            if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
                if (entity.getId() == null) entity.setId(SnowflakeIdGenerator.nextId());
                LocalDateTime now = LocalDateTime.now();
                if (entity.getCreatedAt() == null) entity.setCreatedAt(now);
                if (entity.getUpdatedAt() == null) entity.setUpdatedAt(now);
            } else if (ms.getSqlCommandType() == SqlCommandType.UPDATE) {
                entity.setUpdatedAt(LocalDateTime.now());
            }
        }
        return invocation.proceed();
    }
}
```

### 索引策略

```sql
-- 所有业务表统一索引
CREATE INDEX idx_{table}_created ON {table}(created_at);

-- 高频查询附加索引
CREATE INDEX idx_kb_document_kb ON kb_document(kb_id, status);
CREATE INDEX idx_kb_chunk_doc ON kb_chunk(doc_id, chunk_index);
CREATE INDEX idx_wf_instance_def ON wf_instance(definition_id, status);
CREATE INDEX idx_wf_node_exec_instance ON wf_node_exec(instance_id, status);
CREATE INDEX idx_agent_session_agent ON agent_session(agent_id, user_id);
CREATE INDEX idx_agent_message_session ON agent_message(session_id, parent_id);
CREATE INDEX idx_skill_execution_skill ON skill_execution_log(skill_id, status);
CREATE INDEX idx_model_call_log_model ON model_call_log(model_id, created_at);
CREATE INDEX idx_model_call_log_caller ON model_call_log(caller, created_at);
CREATE INDEX idx_mcp_audit_server ON mcp_audit(server_id, created_at);
CREATE INDEX idx_eval_task_target ON eval_task(target_type, target_id);
```

---

## 项目模块结构

```
AIBase/
├── ai-base-common/             # 公共模块 (DTO/异常/工具/AI安全/BaseEntity/SnowflakeId)
├── ai-base-api/                # Feign接口定义 (服务间契约)
├── ai-base-knowledge/          # knowledge-service (含RAGFlow适配)
├── ai-base-workflow/           # workflow-service
├── ai-base-agent/              # agent-service
├── ai-base-skill/              # skill-service
├── ai-base-mcp-gateway/        # mcp-gateway
├── ai-base-model-gateway/      # model-gateway
├── ai-base-eval/               # eval-service
├── ai-base-platform/           # platform-service (Prompt管理/审批工作台/配置管理)
├── ai-base-gateway/            # API Gateway
├── docs/superpowers/specs/     # 设计文档
└── docker-compose/             # 基础设施编排 (Nacos/PG/Redis/RocketMQ/Milvus/Tempo/Loki/Grafana)
```

---

## 项目启动指南

### 前置条件

- Java 24（本机安装路径：`D:\java24\jdk-24_windows-x64_bin\jdk-24.0.1`）
- Maven 3.6+（本机安装路径：`D:\maven\apache-maven-3.6.3`）
- Docker + Docker Compose（用于基础设施容器）

### 第一步：启动基础设施

```bash
cd docker-compose
docker compose -f infrastructure.yml up -d
```

启动后可用服务：

| 服务 | 地址 | 账号/密码 |
|------|------|----------|
| PostgreSQL | localhost:5432 | aibase / aibase |
| Redis | localhost:6379 | — |
| Nacos | http://localhost:8848/nacos | nacos / nacos |
| Milvus | localhost:19530 | — |
| RocketMQ NameServer | localhost:9876 | — |
| RocketMQ Broker | localhost:10911 | — |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| Grafana Tempo (OTLP) | localhost:4317 | — |
| Loki | localhost:3100 | — |
| Grafana | http://localhost:3000 | admin / admin |

### 第二步：编译项目

```bash
export JAVA_HOME=D:/java24/jdk-24_windows-x64_bin/jdk-24.0.1
mvn clean compile -DskipTests
```

全部 12 个模块将按 reactor 顺序编译。

### 第三步：启动服务（按依赖顺序）

```bash
# 第一批：无依赖的基础服务
mvn spring-boot:run -pl ai-base-knowledge &
mvn spring-boot:run -pl ai-base-workflow &
mvn spring-boot:run -pl ai-base-skill &

# 第二批：依赖基础服务的模块
mvn spring-boot:run -pl ai-base-agent &
mvn spring-boot:run -pl ai-base-mcp-gateway &
mvn spring-boot:run -pl ai-base-model-gateway &
mvn spring-boot:run -pl ai-base-eval &
mvn spring-boot:run -pl ai-base-platform &

# 第三批：API 网关（最后启动）
mvn spring-boot:run -pl ai-base-gateway &
```

### 第四步：访问入口

- **API 网关**：`http://localhost:8081/api/v1/{service}/**`
  - 示例：`POST http://localhost:8081/api/v1/agent/chat`
  - 示例：`POST http://localhost:8081/api/v1/knowledge/search`
- **Nacos 控制台**：`http://localhost:8848/nacos`
- **Grafana 看板**：`http://localhost:3000`

### 环境变量（可选）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DASHSCOPE_API_KEY` | — | 阿里云 DashScope API Key（必填） |
| `PG_HOST` / `PG_PORT` / `PG_DB` | localhost / 5432 / aibase | PostgreSQL 连接 |
| `REDIS_HOST` / `REDIS_PORT` | localhost / 6379 | Redis 连接 |
| `NACOS_SERVER` | localhost:8848 | Nacos 注册中心 |
| `MILVUS_HOST` / `MILVUS_PORT` | localhost / 19530 | Milvus 向量数据库 |
| `ROCKETMQ_NAMESRV` | localhost:9876 | RocketMQ NameServer |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | http://localhost:4317 | OpenTelemetry 链路追踪端点 |
| `EMBEDDING_MODEL` | text-embedding-v3 | Embedding 模型 |

---

## 当前实现状态 (updated 2026-06-12)

### 后端

所有 11 个子模块已完成核心业务引擎实现 + 完善计划 Phase 1-6 全部完成，**140+ 个 Java 文件全部编译通过**，**105+ 个单元测试**，**零 JPA/Hibernate 残留引用**。

各模块当前状态：
- **实体类**：全部创建完成，MyBatis 纯 POJO，BaseEntity 继承体系，枚举使用 MyBatis EnumTypeHandler 自动映射。
- **持久层**：MyBatis 3.0.3 + Druid 1.2.20，SnowflakeAutoFillInterceptor 拦截器替代 @PrePersist/@PreUpdate。**全部 8 个服务共 20+ 个 Mapper 接口已创建**，含 EvalDatasetItemMapper、AnnotationRecordMapper、McpServerRegMapper、McpClientConnMapper、McpToolRegMapper、McpAuditMapper (2026-06-08)。
- **数据库**：DDL 迁移脚本已创建，覆盖全部 27+ 张表。
- **Service 层**：全部 8 个服务已完成核心业务逻辑实现。
- **Controller 层**：完整 REST API，路径 `/api/v1/{module}`，含 Feign 兼容端点。
- **业务引擎 — 5 大核心引擎全部完成** (2026-06-08)：

  **Engine 1 — Agent 工具调用** (`ai-base-agent`)：
  - `Tool.java` — 工具接口（getName/getDescription/getInputSchema/execute）
  - `ToolRegistry.java` — 自动发现所有 Tool Bean，支持按名称过滤 (`getFiltered()`)
  - 5 个 Tool 实现：`KnowledgeSearchTool`（知识库检索）、`CalculatorTool`（数学计算）、`HttpRequestTool`（HTTP 请求）、`SkillTool`（Skill 执行代理）、`McpToolAdapter`（MCP 工具代理）
  - `ReActLoop.java` — 重构为使用 ToolRegistry 替代 Function Map，动态生成工具列表到 System Prompt，支持按 Agent 配置过滤工具集合
  - `AgentServiceImpl.java` — 解析 AgentDef.tools（逗号分隔工具 ID）和 AgentDef.skillIds，构建过滤工具集传入 ReActLoop，返回 `AgentChatResponse`（含 ToolCallRecord 列表）
  - `AgentController.java` — `POST /{id}/chat` 返回 AgentChatResponse，新增 Feign 兼容 `POST /chat`

  **Engine 2 — MCP 协议** (`ai-base-mcp-gateway`)：
  - `McpServerRegMapper.java` / `McpClientConnMapper.java` / `McpToolRegMapper.java` / `McpAuditMapper.java` — 4 个 Mapper 完整 CRUD
  - `McpClientManager.java` — JSON-RPC initialize 握手（protocolVersion 2024-11-05）、connect/disconnect、tools/list 工具发现、tools/call 工具调用
  - `ConnectionPoolManager.java` — @Scheduled 健康检查（30s）、断路器（连续 5 次失败 → UNHEALTHY）、指数退避重连（最多 60s）
  - `McpServiceImpl.java` — 注册→连接→工具发现完整流程，invokeTool 写审计日志，deleteServer 断开+软删除
  - `McpController.java` — `DELETE /servers/{id}`、`POST /tools/{toolId}/invoke`

  **Engine 3 — Skill 执行** (`ai-base-skill`)：
  - `ModelGatewayClient.java` — 调用 model-gateway `/chat` 端点
  - `SkillServiceImpl.java` — 三层分发：PROMPT（模板渲染 `{{param}}` → LLM 调用）、FUNCTION（LLM 函数执行器）、AGENT（委托 agent-service `/chat`）
  - `SkillController.java` — `POST /execute/{skillId}`（Feign 兼容）、`GET /discover?query=`
  - `SkillDefMapper.java` — 按名称/标签搜索

  **Engine 4 — 评估计算** (`ai-base-eval`)：
  - `EvalDatasetItemMapper.java` — CRUD + batchInsert + softDelete
  - `AnnotationRecordMapper.java` — CRUD + update
  - `EvalExecutor.java` — 三种评估策略（RAG/Agent/Skill），调用对应外部服务，判断输出与预期匹配，计算聚合指标
  - `MetricCalculator.java` — MRR、NDCG@K、Precision@K、Recall@K、SuccessRate、ToolCallAccuracy
  - `EvalService.java` / `EvalServiceImpl.java` — 12 个方法覆盖数据集/条目/任务/结果/标注完整 CRUD + 执行
  - `EvalController.java` — 14 个端点，含 Feign 兼容分页变体和 `GET /tasks/{taskId}/results`

  **Engine 5 — 向量搜索** (`ai-base-knowledge`)：
  - `MilvusConfig.java` — `@ConditionalOnProperty` 条件创建 MilvusServiceClient Bean
  - `MilvusKnowledgeRepository.java` — 完整 KnowledgeRepository 实现（createCollection/insertVectors/search/deleteByDocId/dropCollection），IVF_FLAT 索引 + COSINE 度量
  - `KbServiceImpl.java` — 三种检索模式（VECTOR/Milvus + 块内容关联、KEYWORD/PostgreSQL ILIKE、HYBRID/RRF 融合）
  - `DashScopeSearchAdapter.java` — 真实 DashScope 搜索 API 调用
  - `KbChunkMapper.java` — selectByIds（批量）、keywordSearch
  - `IngestPipeline.java` — 构造器注入 `Optional<KnowledgeRepository>`

- **安全基础 — Phase 1 完成** (2026-06-08)：
  - `ApiKeyAuthFilter.java` — OncePerRequestFilter，校验 `X-Api-Key` 头，缺或无效返回 401
  - `ApiKeyStore.java` / `PropertiesApiKeyStore.java` — API Key 存储接口 + 配置属性加载实现 (`aibase.security.keys`)
  - `WebConfig.java` — 注册 ApiKeyAuthFilter Bean，拦截 `/api/*` 路径
  - `AuthGatewayFilterFactory.java` — Gateway 层 API Key 存在性检查过滤器
  - `RateLimitFilterFactory.java` — 内存 Token Bucket 限流器（容量 100，填充 10/s），超阈值返回 429
  - `PromptInjectionDetector.java` / `SensitiveDataMasker.java` — 添加 `@Component`，可注入使用
  - **前端适配** (2026-06-09)：`client.ts` 请求拦截器已添加 `X-Api-Key` 头，默认 `aibase-dev-key-2024`，可通过 `localStorage.setItem('apiKey', ...)` 覆盖。不携带 API Key 的请求将被拦截返回 401。

- **Agent 高级模式 — Phase 3 完成** (2026-06-08)：
  - `ContextWindowManager.java` — 滑动窗口裁剪历史消息，Token 预算管理（默认 8000 tokens）
  - `ConversationTree.java` — 对话树：parentId 链路径追溯、分支点检测（多子节点）
  - `GraphBridge.java` — Agent→Workflow 桥接：提交工作流实例 + 轮询等待完成（最多 4 分钟）
  - `WorkflowServiceClient.java` — RestClient 封装，调用 workflow-service `/start` + `/instances/{id}`
  - `NegotiationEngine.java` — 多 Agent 协商引擎：PROPOSE→VOTE→ARBITRATE 三阶段 LLM 驱动
  - `AgentServiceImpl.java` — 三模式路由：REACT→ReActLoop / GRAPH→GraphBridge / NEGOTIATION→NegotiationEngine
  - `AgentController.java` — 新增 `POST /sessions/{sessionId}/branch`（对话分支）+ `GET /sessions/{sessionId}/tree`（分支点列表）

- **原有业务引擎**：
  - `WorkflowExecutor.java` — DAG 拓扑排序执行引擎 (Kahn's algorithm)，**已扩展至 9 种节点类型全支持** (2026-06-08 Phase 2)：
    - START / END / SKILL / AGENT（原有）
    - CONDITION（条件表达式路由 `==`/`!=`/truthy + BFS 分支阻断）
    - PARALLEL（CompletableFuture 并发扇出 + virtual threads）
    - LLM_CALL（调用 model-gateway，支持 `{{变量}}` 模板替换）
    - CODE（JDK ScriptEngineManager JavaScript 沙箱 + 超时中断）
    - WAIT（PAUSED 状态 + `POST /instances/{id}/signal` 恢复端点）
    - KNOWLEDGE（调用 knowledge-service `/search`）
    - 节点级重试（解析 retryPolicy.maxRetries + 指数退避 1s→2s→4s→...）
  - `ModelGatewayClient.java` (workflow) — 封装 model-gateway `/chat` 调用
  - `DagParser.java` — ParsedNode 新增 `config` 字段支持节点级配置
  - `DashScopeProvider.java` / `OpenAIProvider.java` — 真实 HTTP 调用模型 API
- **Feign 接口**：7 个服务间契约接口（Agent/Skill/Knowledge/Workflow/MCP/Eval/ModelGateway）
- **配置文件**：application-base.yml + 各服务 application.yml + Docker Compose 基础设施编排

### 前端

- **项目路径**：`ai-base-frontend/`
- **技术栈**：React 18 + TypeScript 5 + Vite 5 + Ant Design 5 + Zustand 4 + React Router 6 + Axios 1
- 24 条路由全部懒加载，8 个 API 模块 + 8 个 Zustand store + 17 个页面，TypeScript 编译零错误
- **新增** (2026-06-09)：
  - **Workflow DAG 画布编辑器**（`@xyflow/react` v12）：11 种节点类型拖拽式可视化编辑，节点配置面板（类型特定表单），MiniMap + 缩放控制，DAG 保存至后端
  - **Agent/Skill/MCP/Model CRUD Modal**：各模块新建/编辑/删除完整支持，Skill 按层级（PROMPT/FUNCTION/AGENT）动态表单，列表页 + 详情页均有操作入口
  - **Workflow CRUD**：新建/删除工作流，列表页增强

- **新增** (2026-06-12)：
  - **Knowledge 检索交互**：KbDetailPage 新增搜索框 + 策略选择器 + 结果列表含相关度评分 + 内容预览 Modal + 关键词高亮
  - **Eval 评估执行**：EvalTaskListPage 新增执行按钮 + 创建任务 Modal + 成功率列；EvalExecutor.judge() 增强为 token-overlap 模糊匹配（60% 阈值）
  - **Skill 模板渲染增强**：renderTemplate() 支持嵌套参数 `{{parent.child}}` + 未解析占位符清除
  - **Platform 审批链**：ApprovalRecord 新增 chainStep/totalSteps；approve() 多步审批链自动递增 + 最终步骤自动发布；SlaTracker 48h 超时自动升级 ESCALATED
  - **E2E 测试基建**：Playwright + 3 个 spec 文件（chat/knowledge/agent）+ playwright.config.ts + npm scripts
- **Sprint 1 补齐** (2026-06-12)：
  - **多格式文档解析**：Apache Tika 2.9.2 集成，PDF/DOCX/PPTX/TXT/MD/HTML/CSV 自动解析；新增 `POST /kb/{id}/upload` MultipartFile 端点；FileSystemConnector 改用 Tika
  - **3 新工作流节点**：QUESTION_CLASSIFIER（LLM 分类器）、VARIABLE_ASSIGNER（变量赋值+类型转换）、HTTP_REQUEST（完整 REST 调用）
  - **前端上传修复**：DocumentUploadPage 从 stub → 真实 FormData 上传 + 进度跟踪
- **后续计划**：Chatflow 对话流引擎 → 可视化 Prompt IDE → OAuth/SSO，详见 [docs/dify-comparison.md](docs/dify-comparison.md) 和 [plans/lexical-hopping-hammock.md](../plans/lexical-hopping-hammock.md)

### 实施进度 (as of 2026-06-12)

**已完成（核心功能）：**
- 全部 9 个数据库迁移（V001-V009），27+ 张表
- **5 大核心业务引擎全部实现并编译通过**（+21 新建文件，+18 修改文件）
- Agent 模块：ReActLoop + ToolRegistry + 5 个 Tool 实现 + AgentChatResponse
- MCP 协议：JSON-RPC 握手 + 工具发现/调用代理 + 连接池管理 + 断路器
- Skill 执行：三层分发（PROMPT/FUNCTION/AGENT）+ 模板渲染 + 执行日志
- 评估计算：EvalExecutor + MetricCalculator + 数据集/标注完整 CRUD
- 向量搜索：MilvusKnowledgeRepository + 混合检索 + RRF 融合 + DashScope 搜索适配器
- Workflow 模块：DAG 执行器 + WfNodeExec 跟踪
- Model Gateway：OpenAI + DashScope 提供商 + /chat + /embed 端点
- Common 模块：BaseEntity、ApiResponse、SnowflakeIdGenerator、安全工具、GlobalExceptionHandler
- 前端：24 路由 + 8 API 模块 + 8 Zustand stores + 17 页面 + TypeScript 零错误

### 部署记录 (2026-06-09)

**MCP Gateway 启动失败修复**：
- **问题**：`@MapperScan("com.datang.aibase")` 范围过宽，MyBatis 将 `ExportableTool` 接口误识别为 Mapper 并创建代理，调用时无法找到 XML 映射语句
- **修复**：`@MapperScan` 精确限定为 `{"com.datang.aibase.mcp.mapper", "com.datang.aibase.mcpgateway.mapper"}`；`LocalToolRegistry` 构造函数从 `List<ExportableTool>` 改为 `ObjectProvider<ExportableTool>`

**Docker 网络隔离修复**：
- **问题**：MCP Gateway 容器在错误网络，无法解析 PostgreSQL 的 `postgres` 主机名，HikariPool 首次 API 调用时懒加载失败
- **修复**：手动 `docker network connect --alias mcp-gateway aibase_default` + `app.yml` 添加 `networks.default.external: true` 指向 `aibase_default`
- **教训**：从子目录执行 `docker-compose` 时项目名取自目录名，会创建隔离网络。务必从项目根目录执行，或显式声明外部网络

**前后端联调**：
- 全部 8 个后端服务 + API Gateway + 前端 Nginx 均已部署到服务器 `10.139.11.100`，验证通过
- `curl` 验证命令需携带 `-H "X-Api-Key: aibase-dev-key-2024"`，否则返回 401

**前端 DAG 编辑器 + CRUD Modal 部署 (2026-06-09)**：
- 安装 `@xyflow/react` v12（React Flow 画布库），新增 6 个组件文件
- WorkflowEditorPage 重写为全功能 DAG 编辑器：左侧节点面板（11 种可拖拽节点）、中间画布（连线/缩放/MiniMap）、右侧配置抽屉（类型特定表单）、顶部保存按钮
- 4 个 CRUD Modal：AgentCreateModal（含 systemPrompt/model/coordinationMode/tools/skills/kbs 字段）、SkillCreateModal（按层级动态表单 PROMPT/FUNCTION/AGENT）、McpCreateModal（serverType/transport/endpoint/authConfig）、ModelCreateModal（provider/endpoint/apiKeyRef/maxTokens/capabilities/priority）
- 5 个列表页新增编辑/删除图标（Popconfirm），5 个详情页新增编辑/删除按钮
- 所有 5 个 API 模块（agent/skill/mcp/model/workflow）新增 create/update/delete 函数，5 个 Zustand store 新增 create/update/remove action
- 前端 build 12.35s，React Flow chunk 67 kB gzipped，TypeScript 零错误
- 已部署至服务器 `10.139.11.100`

**Milvus 持续重启修复 (2026-06-09)**：
- **问题**：`.env` 中 `MINIO_ROOT_PASSWORD=minioadmin_change_me`，但 Milvus 容器未配置 MinIO 凭据环境变量，默认使用 `minioadmin/minioadmin` 连接 MinIO，导致签名验证失败（`The request signature we calculated does not match`），blob bucket 无法创建，datacoord/querycoord 内部组件初始化失败，容器 7 秒内崩溃重启
- **修复**：在 `docker-compose/infrastructure.yml` Milvus 服务中添加 `MINIO_ACCESS_KEY_ID: minioadmin` 和 `MINIO_SECRET_ACCESS_KEY: ${MINIO_ROOT_PASSWORD:-minioadmin}` 环境变量，凭据与 MinIO 容器的 `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD` 保持一致。同步清理本地仓库的 `infrastructure.yml`
- **附带清理**：修改凭据后需清理 etcd 中旧 Milvus 实例注册数据（`etcdctl del --prefix /by-dev`），否则新实例与旧组件 ID 不匹配会报 `node not match` 错误
- 修复后健康检查 `/healthz` 返回 OK，容器稳定运行不再重启

### 待实现（按优先级）

1. ~~**P4 — 前端 Chat 页面接入真实 Agent API**~~ ✅ 已完成 (2026-06-09: chatStore 调用 `/agent/chat` 端点，ChatController 转发至 agent-service)
2. ~~**P4 — 前端 Knowledge 检索交互**~~ ✅ 已完成 (2026-06-12: 搜索框 + 策略选择器 + 结果列表含相关度评分 + 内容预览 Modal + 关键词高亮)
3. ~~**P4 — 前端 Eval 评估任务执行与结果对比**~~ ✅ 已完成 (2026-06-12: 执行按钮 + 创建任务 Modal + 成功率展示 + judge 模糊匹配增强)
4. **P5 — 前端 ConnectorConfigCard/SearchEngineConfigCard**（知识库连接器/搜索引擎配置 UI）
5. **P5 — 前端 SearchBar + useRequest/usePagination 通用组件**

---

## 完善计划 (6 Phases)

> 5 大核心业务引擎主干路径已通，但仍有大量细节功能缺失。此计划针对三类问题（完全缺失/有壳缺肉/横切面缺失），按依赖关系和场景优先级排序。

### 执行顺序

```
Phase 1 ✅ (安全基础) ─────────────────────────────┐
Phase 2 ✅ (Workflow 全节点) ───┐                   │
Phase 3 ✅ (Agent 高级模式) ────┤                   │
  ├── GraphBridge ── 依赖 ────┘                   │
  ├── ContextWindowManager (独立)                │
  └── Negotiation (独立)                         │
Phase 4 ✅ (MCP Server + Knowledge 连接器 + Skill 加固) ── 独立，三者可并行
Phase 5 ✅ (Platform 增强 + Gateway 收尾) ───── 独立
Phase 6 ✅ (测试体系) ─── 105+ 测试用例，覆盖所有 8 个服务模块
```

**最佳路径：1 → 2 → 3 → 4+5 并行 → 6 ✅ 全部完成**

---

### Phase 1: 安全基础 ✅ 已完成 (复杂度: MEDIUM)

> 当前所有端点完全开放。不解决则任何场景无法上线。

#### 1.1 API Key 管理
- **新建** `ai-base-common/.../security/ApiKeyAuthFilter.java` — OncePerRequestFilter，校验 `X-Api-Key` 头
- **新建** `ai-base-common/.../security/ApiKeyStore.java` — 接口，默认从配置加载 key，可扩展 DB 实现
- **修改** `ai-base-common/.../config/WebConfig.java` — 注册 Filter

#### 1.2 Gateway 认证 + 速率限制
- **新建** `ai-base-gateway/.../filter/AuthGatewayFilterFactory.java` — GatewayFilter，读取并转发 API Key
- **新建** `ai-base-gateway/.../filter/RateLimitFilterFactory.java` — 基于 Redis + Token Bucket 的简单限流
- **修改** `ai-base-gateway/.../application.yml` — default-filters 配置
- **修改** `ai-base-gateway/pom.xml` — 添加 `spring-boot-starter-data-redis-reactive`

#### 1.3 安全工具接入
- **修改** `PromptInjectionDetector.java`, `SensitiveDataMasker.java` — 添加 `@Component`
- **修改** 关键 Controller — 调用 `injectionDetector.detectInjection()`

#### 验收标准
- [x] 不带 X-Api-Key 返回 401
- [x] Prompt 注入检测拦截已知攻击
- [x] 超阈值限流返回 429

---

### Phase 2: Workflow 全 9 种节点类型 ✅ 已完成 (复杂度: HIGH)

> 当前只处理 START/END/SKILL/AGENT。CONDITION 和 PARALLEL 是 DAG 核心，无则工作流只做串行。

#### 2.1 CONDITION — 条件路由
- **修改** `WorkflowExecutor.java` — 解析 config 中表达式，按边 label 分发

#### 2.2 PARALLEL — 并行扇出/汇聚
- **修改** `WorkflowExecutor.java` — CompletableFuture 并行执行子节点，等待全部完成

#### 2.3 LLM_CALL — 直接 LLM 调用
- **新建** `workflow/client/ModelGatewayClient.java`
- **修改** `WorkflowExecutor.java` — 调用 model-gateway `/chat`

#### 2.4 CODE — 脚本沙箱
- **修改** `WorkflowExecutor.java` — JDK ScriptEngineManager 执行 JS，超时中断

#### 2.5 WAIT — 事件等待
- **修改** `WorkflowExecutor.java` / `WfInstance.java` — 支持 PAUSED 状态 + `/instances/{id}/signal` 回调端点

#### 2.6 节点级重试
- **修改** `WorkflowExecutor.java` — 读取 retryPolicy，指数退避

#### 验收标准
- [x] CONDITION 按表达式选分支，PARALLEL 并行执行合并结果
- [x] CODE 节点执行 JS 脚本 + 超时中断
- [x] WAIT 暂停 → signal 恢复

---

### Phase 3: Agent 高级模式 + 上下文管理 ✅ 已完成 (复杂度: HIGH)

#### 3.1 ContextWindowManager
- **新建** `agent/memory/ContextWindowManager.java` — 滑动窗口 + 自动摘要旧消息

#### 3.2 ConversationTree
- **新建** `agent/memory/ConversationTree.java` — 分支/路径/列出分支点
- **修改** `AgentController.java` — `POST /sessions/{sessionId}/branch`

#### 3.3 Graph Bridge — Agent 转 Workflow
- **新建** `agent/bridge/GraphBridge.java` — 向 workflow-service 提交实例
- **新建** `agent/client/WorkflowServiceClient.java`
- **修改** `AgentServiceImpl.chat()` — `coordinationMode: GRAPH` 时调 GraphBridge

#### 3.4 Negotiation — 多 Agent 协商
- **新建** `agent/negotiation/NegotiationEngine.java` — PROPOSE→VOTE→ARBITRATE，用 RocketMQ 做消息通道
- **修改** `AgentServiceImpl.chat()` — `NEGOTIATION` 时调 NegotiationEngine
- **修改** `pom.xml` — 添加 RocketMQ starter

#### 验收标准
- [x] 长对话自动摘要旧消息，Token 不溢出
- [x] Graph Bridge 提交 Workflow 并获得结果
- [x] 3 Agent 完成一轮协商

---

### Phase 4: MCP Server + Knowledge 连接器 + Skill 加固 ✅ 已完成 (复杂度: MEDIUM~HIGH)

> 三项独立工作，可并行推进。

#### 4A. MCP Server 侧 (复杂度: HIGH)
- **新建** `mcpgateway/server/ExportableTool.java` — 接口
- **新建** `mcpgateway/server/LocalToolRegistry.java` — 收集 ExportableTool Bean
- **新建** `mcpgateway/server/McpServerTransport.java` — WebFlux SSE transport + JSON-RPC 2.0
- **新建** `mcpgateway/server/McpServerController.java` — `GET /mcp/sse`, `POST /mcp/message`

#### 4B. Knowledge 连接器 (复杂度: MEDIUM)
- **新建** `connector/DatabaseConnector.java` — JDBC 提取数据
- **新建** `connector/ApiConnector.java` — 对接第三方 API（飞书/Confluence）
- **新建** `repository/RagFlowKnowledgeRepository.java` — 适配 RAGFlow

#### 4C. Skill 加固 (复杂度: MEDIUM)
- **修改** `SkillServiceImpl.java` — Function 技能优先 GraalJS 沙箱，后备 LLM
- **修改** `SkillServiceImpl.java` — Redis 缓存 + pub/sub 热加载
- **修改** `SkillVersionMapper.java` — 语义版本比较 + promote

#### 验收标准
- [x] MCP Client 连接底座 MCP Server 并调用工具
- [x] DB 连接器提取表数据并摄入
- [x] GraalJS 沙箱执行脚本，超时中断
- [x] Skill 修改后 Redis 缓存自动失效

---

### Phase 5: Platform 增强 + Gateway 收尾 ✅ 已完成 (复杂度: MEDIUM)

#### 5.1 审批工作流增强
- **修改** `PlatformServiceImpl.java` — 多步骤审批链 + 转派
- **新建** `platform/service/SlaTracker.java` — SLA 超时告警

#### 5.2 Prompt 版本管理
- **修改** `PlatformServiceImpl.java` — publish/rollback
- **修改** `PromptVersionMapper.java` — update/setCurrent

#### 5.3 ChatController 接入 AI
- **修改** `ai-base-api/.../ChatController.java` — 替换 echo 为真实 agent 调用

#### 5.4 Gateway 过滤器
- **新建** `LoggingFilterFactory.java` — 请求耗时/状态码/TraceID 日志
- **新建** `ObservabilityFilterFactory.java` — TraceID 注入响应头

#### 验收标准
- [x] 3 步审批链可用，SLA 超时告警
- [x] Prompt 可发布/回滚
- [x] `POST /api/chat/send` 返回真实 AI 回复

---

### Phase 6: 测试体系 ✅ 已完成 (复杂度: HIGH)

> 零测试是最大风险。按模块优先级分批补。

| 批次 | 模块 | 重点 | 目标覆盖率 |
|------|------|------|-----------|
| 1 | common | ApiResponse, SnowflakeIdGenerator, 安全工具, GlobalExceptionHandler | 90% |
| 2 | agent | ToolRegistry, ReActLoop, Tool 实现, AgentServiceImpl | 80% |
| 3 | knowledge | KbServiceImpl.search(), IngestPipeline, MilvusKnowledgeRepository | 80% |
| 4 | skill | SkillServiceImpl(三层分发), 模板渲染 | 80% |
| 5 | eval | EvalExecutor, MetricCalculator, EvalServiceImpl | 80% |
| 6 | mcp-gateway | McpClientManager, ConnectionPoolManager | 80% |
| 7 | workflow | WorkflowExecutor(全节点), DagParser | 80% |
| 8 | platform | ApprovalService, PromptVersionService | 70% |

#### 基础设施
- 所有模块 pom.xml 添加 `spring-boot-starter-test` + `junit-jupiter` + `mockito-core` + `assertj-core` + `testcontainers`
- 各模块 `src/test/resources/application-test.yml`

---

### 文件清单汇总

#### 新建 (~35 个)

| Phase | 模块 | 文件 |
|------|------|------|
| P1 | common | `security/ApiKeyAuthFilter.java`, `security/ApiKeyStore.java` |
| P1 | gateway | `filter/AuthGatewayFilterFactory.java`, `filter/RateLimitFilterFactory.java` |
| P2 | workflow | `client/ModelGatewayClient.java` |
| P3 | agent | `memory/ContextWindowManager.java`, `memory/ConversationTree.java` |
| P3 | agent | `bridge/GraphBridge.java`, `client/WorkflowServiceClient.java` |
| P3 | agent | `negotiation/NegotiationEngine.java` |
| P4 | mcp-gateway | `server/ExportableTool.java`, `server/LocalToolRegistry.java`, `server/McpServerTransport.java`, `server/McpServerController.java` |
| P4 | knowledge | `connector/DatabaseConnector.java`, `connector/ApiConnector.java`, `repository/RagFlowKnowledgeRepository.java` |
| P5 | platform | `service/SlaTracker.java` |
| P5 | gateway | `filter/LoggingFilterFactory.java`, `filter/ObservabilityFilterFactory.java` |
| P6 | 各模块 | `src/test/...` (15+ 测试类, 105+ 测试用例) |

#### 修改 (~25 个)

| Phase | 模块 | 文件 | 要点 |
|------|------|------|------|
| P1 | common | `WebConfig.java`, `PromptInjectionDetector.java`, `SensitiveDataMasker.java` | 注册 Filter + @Component |
| P1 | gateway | `application.yml`, `pom.xml` | 过滤器配置 + redis 依赖 |
| P1 | 各模块 | Controller | 注入检测 |
| P2 | workflow | `WorkflowExecutor.java`, `WfInstance.java` | 5 节点 + 重试 + 暂停状态 |
| P3 | agent | `AgentServiceImpl.java`, `AgentController.java`, `pom.xml` | 模式分发 + 分支端点 + RocketMQ |
| P4 | skill | `SkillServiceImpl.java`, `pom.xml` | GraalJS + Redis |
| P4 | knowledge | `pom.xml`/配置 | RAGFlow 端点 |
| P5 | platform | `PlatformServiceImpl.java`, `ApprovalRecordMapper.java`, `PromptVersionMapper.java` | 审批链 + 发布/回滚 |
| P5 | api | `ChatController.java` | 真实 AI 调用 |
| P6 | 各模块 | `pom.xml` | 测试依赖 |

---

## 未来扩展点

| 扩展点 | 当前预留 | 扩展方式 |
|--------|---------|---------|
| 其他知识库（ES/OpenSearch） | KnowledgeRepository接口 | 实现新Repo + 注册Bean |
| 新LLM模型 | model-gateway适配层 | 新增适配器 |
| 新MCP版本 | ProtocolAdapter层 | 新增适配器 |
| 第三方数据源 | DataSourceConnector接口 | 实现connector + 注册 |
| 新搜索引擎 | SearchEngineAdapter接口 | 实现adapter + 注册 |
| 新通知渠道（企微/钉钉/飞书） | NotifyChannel接口 | 实现channel + 注册 |
| 多语言Agent | 国际化Prompt模板 | Skill多语言版本 |
| AI Agent SDK | mcp-gateway Server能力 | 第三方通过MCP协议接入 |

---

## 上层应用场景

底座之上建设 **9 个业务场景**，通过底座暴露的统一 API / MCP 协议调用各项能力。

### 场景总览与底座能力映射

| # | 场景 | 核心特征 | 依赖底座能力 |
|---|------|---------|------------|
| 1 | 智能写作 | 5类模板、结构化生成 | Agent(reAct) + Skill(Prompt模板) + Workflow(条件路由) + Knowledge(RAG) |
| 2 | 智能立项评审 | 文档理解、多维打分 | Agent(ReAct) + Knowledge(文档检索) + Workflow(评审流程) |
| 3 | 任务书辅助评审 | 合规检查、对比分析 | Agent(ReAct) + Knowledge(标准库检索) + Skill(规则函数) |
| 4 | 智能任务管控 | 进度追踪、风险预警 | Agent(协商) + Workflow(状态流转) + Skill(通知/告警) |
| 5 | 智能验收辅助 | 多维度验收、报告生成 | Agent(ReAct) + Workflow(验收流程) + Knowledge(验收标准) |
| 6 | 项目分类画像 | 数据分析、标签体系 | Agent(ReAct) + Skill(数据处理函数) + Workflow(并行分析) |
| 7 | 资料完整性检查 | 清单核对、缺失提醒 | Agent(ReAct) + Skill(文档解析/规则检查) + Knowledge(清单标准) |
| 8 | 研发经费合规审查 | 规则匹配、异常检测 | Agent(ReAct) + Skill(规则引擎函数) + Knowledge(政策法规库) |
| 9 | 智能报表生成 | 数据聚合、可视化 | Agent(ReAct) + Workflow(数据ETL流程) + Skill(图表生成函数) |

### 各场景详细说明

#### 1. 智能写作（5类模板）

| 模板 | 输入 | 输出 | 写作特点 |
|------|------|------|---------|
| 项目申报书 | 项目基本信息、技术指标 | 完整申报书 | 结构化嵌套、章节间关联 |
| 任务书 | 项目标的、分工、里程碑 | 标准任务书 | 表格+段落混合、甘特图 |
| 实施方案 | 项目目标、资源、约束 | 实施方案 | 步骤化、带时间线和风险预案 |
| 结题报告 | 项目全过程数据 | 结题报告 | 数据驱动、成果对比分析 |
| 执行情况报告 | 阶段性进度数据 | 执行报告 | 进展/偏差/措施三段式 |

**底座支撑方式**：
- 每类模板封装为一个 PromptSkill (Layer 1)，定义章节结构和写作风格
- Agent 通过 ReAct 模式：理解用户输入 → 检索知识库补充背景 → 调用Skill逐段生成 → 整合校验
- 写作流程复杂时通过 Workflow（条件路由选择模板、多章节并行生成）

#### 2. 智能立项评审

- 输入：申报书全文
- 输出：多维度评分（技术可行性/创新性/经费合理性/团队能力）+ 评审意见
- 底座支撑：Agent检索历史评审案例和标准知识库 → Workflow定义评审维度流 → Skill执行分项评分函数

#### 3. 任务书辅助评审

- 输入：任务书文档
- 输出：合规性检查报告、与模板标准的差异对比
- 底座支撑：Skill做结构化规则比对（里程碑合理性、预算合规），Agent做语义理解补充

#### 4. 智能任务管控

- 输入：项目进度数据、里程碑状态
- 输出：进度预警、偏差分析、建议措施
- 底座支撑：Workflow管理管控状态流转（正常→预警→升级），Skill做阈值检测和通知触发，多Agent协商对复杂偏差情况讨论

#### 5. 智能验收辅助

- 输入：项目成果材料、验收标准
- 输出：验收评估报告、逐项达标判断
- 底座支撑：Knowledge检索验收标准库，Workflow按验收条目并行执行检查，Agent汇总评分

#### 6. 项目分类画像

- 输入：项目全量数据
- 输出：多维度分类标签、项目画像报告
- 底座支撑：Skill执行统计分析和标签计算，Workflow并行处理多个分类维度，Agent整合生成画像描述

#### 7. 资料完整性检查

- 输入：项目文件清单
- 输出：缺失项报告、补交提醒
- 底座支撑：Skill做文件格式/内容检测规则匹配，Knowledge检索资料清单标准模板，Agent生成补交说明

#### 8. 研发经费合规审查

- 输入：经费使用明细
- 输出：合规性报告、异常支出标注、整改建议
- 底座支撑：Knowledge存储政策法规/经费管理办法，Skill做规则匹配和阈值检测，Agent对模糊边界做语义判断

#### 9. 智能报表生成

- 输入：多源业务数据
- 输出：多类型报表（统计报表/趋势分析/对比报表）
- 底座支撑：Workflow编排数据ETL流程，Skill执行数据处理和图表生成，Agent负责需求理解和结果解释

### 场景能力复用矩阵

```
                  知识库  工作流  Agent  Skill  Prompt模板  评估
智能写作            ✓       ✓      ✓      ✓       ✓
智能立项评审        ✓       ✓      ✓      ✓                ✓
任务书辅助评审      ✓              ✓      ✓                ✓
智能任务管控                ✓      ✓      ✓
智能验收辅助        ✓       ✓      ✓                      ✓
项目分类画像                ✓      ✓      ✓
资料完整性检查      ✓              ✓      ✓
研发经费合规审查    ✓              ✓      ✓                ✓
智能报表生成                ✓      ✓      ✓
```

---

## 上层Agent建设指南

上层Agent通过底座能力进行**声明式组装**，不编写服务端代码，只做配置和前端。

```
┌─────────────────────────────────────────────────────────────┐
│                    上层 Agent（配置组装）                      │
│                                                             │
│  1. 创建知识库    → knowledge-service (API/管理界面)          │
│  2. 注册Skill     → skill-service (API/管理界面)              │
│  3. 定义Agent     → agent-service (API/管理界面)              │
│  4. 编排工作流    → workflow-service (API/管理界面)           │
│  5. 构建评估集    → eval-service (API/管理界面)               │
│  6. 对接前端      → 通过 API Gateway 调用 Agent 会话接口       │
└─────────────────────────────────────────────────────────────┘
```

### 开发集成方式

上层 9 个场景独立项目 `AIApps`，与底座 `AIBase` 分离。核心思路：**底座提供能力引擎，上层场景只做配置+前端，不写服务端代码**。

**架构关系**：

```
AIApps/                          ← 独立项目，与 AIBase 分离
├── ai-apps-common/              ← 场景间共享的通用 Skill/工具
├── ai-apps-smart-writing/       ← 场景1：智能写作
├── ai-apps-project-review/      ← 场景2：智能立项评审
├── ai-apps-task-review/         ← 场景3：任务书辅助评审
├── ai-apps-task-control/        ← 场景4：智能任务管控
├── ai-apps-acceptance/          ← 场景5：智能验收辅助
├── ai-apps-project-profiling/   ← 场景6：项目分类画像
├── ai-apps-completeness-check/  ← 场景7：资料完整性检查
├── ai-apps-fund-compliance/     ← 场景8：研发经费合规审查
└── ai-apps-report-generator/    ← 场景9：智能报表生成

AIBase/                          ← 底座（当前项目）
├── knowledge / skill / workflow / agent / mcp-gateway / ...
└── 通过统一 API + MCP 协议向上暴露能力
```

**每个场景项目只包含 5 类内容**：

| 内容 | 说明 | 存放位置 |
|------|------|---------|
| Agent 定义 JSON | 描述 Agent 角色、模式（ReAct/Graph）、关联的 Skill | `agents/` |
| Workflow DSL | 定义工作流节点、条件路由、并行分支 | `workflows/` |
| Skill 定义 | Prompt 模板 + 函数代码（规则匹配/数据处理等） | `skills/` |
| 评估数据集 | 测试用例，用于回归评估 | `eval-datasets/` |
| 前端页面 | 场景专属交互界面 | `frontend/` |

**建设路径（6 步声明式组装）**：

```
1. 创建知识库   → knowledge-service    (API)
2. 注册 Skill   → skill-service        (API)
3. 定义 Agent   → agent-service        (API)
4. 编排工作流   → workflow-service     (API)
5. 构建评估集   → eval-service         (API)
6. 对接前端     → API Gateway → Agent 会话接口
```

**关键设计意图**：

- **底座建一次，场景复用**：9 个场景共享同一套知识库/工作流/Agent/Skill 能力，只是组合方式不同，通过底座 API 提交配置即可完成部署。
- **不写服务端代码**：所有业务逻辑通过声明式配置完成，场景项目无后端代码。
- **场景与底座解耦**：AIApps 和 AIBase 为独立仓库，场景升级不影响底座，底座升级所有场景自动受益。

---

### 示例：智能写作-项目申报书

以最复杂的场景为例，演示完整建设路径。

#### 步骤1：建设知识库

在 knowledge-service 创建 `kb_proposal_writing`：

```yaml
connectors:
  - type: upload        # 上传历史优秀申报书（PDF/Word）
  - type: fs            # 批量导入申报书模板库
  - type: api           # 同步飞书文档中的写作规范
search_engines:
  - type: dashscope     # 搜索最新政策要求、技术前沿动态
```

#### 步骤2：注册Skill

| 层级 | Skill名称 | 作用 |
|------|----------|------|
| Layer1 Prompt | `proposal-section-writer` | 各章节写作模板（技术路线/经费预算/团队成员/预期成果） |
| Layer2 Function | `proposal-formatter` | 格式校验（字号/行距/页边距/图表编号） |
| Layer2 Function | `budget-table-generator` | 经费预算表自动计算与生成 |
| Layer2 Function | `completeness-checker` | 必填章节完整性检查 |
| Layer3 Agent | `proposal-reviewer` | 子Agent，模拟评审专家审读草稿并给出修改建议 |

#### 步骤3：定义Agent

```json
{
  "name": "申报书写作Agent",
  "systemPrompt": "你是科技项目申报书写作专家，擅长根据项目基本信息生成完整的申报书。你需要：1) 检索历史优秀申报书作为参考 2) 逐章生成内容 3) 调用格式校验确保合规 4) 调用评审子Agent进行自审",
  "model": "qwen-max",
  "coordinationMode": "graph",
  "tools": ["proposal-formatter", "budget-table-generator", "completeness-checker"],
  "skills": ["proposal-section-writer", "proposal-reviewer"],
  "knowledgeBases": ["kb_proposal_writing"]
}
```

#### 步骤4：编排工作流

```
用户输入项目基本信息
        │
        ▼
  ┌─────────────┐
  │ 意图理解     │  LLM_CALL: 解析项目类型/领域/规模
  └──────┬──────┘
         │
         ▼
  ┌─────────────┐
  │ 知识检索     │  KNOWLEDGE: 检索同类优秀申报书 + 最新政策
  └──────┬──────┘
         │
         ▼
  ┌──────────────────────────────────────────────────┐
  │              并行生成各章节 (PARALLEL)              │
  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐           │
  │  │技术路线│ │经费预算│ │团队成员│ │预期成果│  ...      │
  │  └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘           │
  │     │        │        │        │                │
  │  每个节点: SKILL(proposal-section-writer)        │
  └─────┼────────┼────────┼────────┼────────────────┘
         │        │        │        │
         └────────┴─────┬──┴────────┘
                        ▼
              ┌─────────────────┐
              │ 章节整合         │  LLM_CALL: 整合各章节，统一风格
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ 格式校验         │  SKILL(proposal-formatter)
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ 完整性检查       │  SKILL(completeness-checker)
              └────────┬────────┘
                       │
                  ┌────▼────┐
                  │ 不完整？  │ CONDITION
                  └─┬─────┬─┘
              补充    │     │ 完整
              ┌───────┘     └───────┐
              ▼                     ▼
        回到"并行生成"         ┌─────────────┐
                              │ 自审         │  AGENT(proposal-reviewer)
                              └──────┬──────┘
                                     │
                                     ▼
                              ┌─────────────┐
                              │ 人工审核      │  WAIT: 提交审批工作台
                              └──────┬──────┘
                                     │
                                     ▼
                              ┌─────────────┐
                              │ 输出终稿      │
                              └─────────────┘
```

工作流JSON DSL定义：

```json
{
  "name": "申报书生成流程",
  "nodes": [
    {"id":"parse","type":"LLM_CALL","config":{"prompt":"解析项目类型/领域/规模"}},
    {"id":"search","type":"KNOWLEDGE","config":{"kbId":"kb_proposal_writing","topK":10}},
    {"id":"parallel","type":"PARALLEL","children":[
      {"id":"tech_route","type":"SKILL","config":{"skillId":"proposal-section-writer","params":{"section":"技术路线"}}},
      {"id":"budget","type":"SKILL","config":{"skillId":"proposal-section-writer","params":{"section":"经费预算"}}},
      {"id":"team","type":"SKILL","config":{"skillId":"proposal-section-writer","params":{"section":"团队成员"}}},
      {"id":"outcome","type":"SKILL","config":{"skillId":"proposal-section-writer","params":{"section":"预期成果"}}}
    ]},
    {"id":"merge","type":"LLM_CALL","config":{"prompt":"整合各章节，统一语言风格"}},
    {"id":"format_check","type":"SKILL","config":{"skillId":"proposal-formatter"}},
    {"id":"completeness","type":"SKILL","config":{"skillId":"completeness-checker"}},
    {"id":"route","type":"CONDITION","config":{"expr":"completeness == 'INCOMPLETE' ? 'parallel' : 'review'"}},
    {"id":"review","type":"AGENT","config":{"agentId":"proposal-reviewer"}},
    {"id":"approval","type":"WAIT","config":{"title":"申报书审批","assigneeRole":"PM"}},
    {"id":"output","type":"LLM_CALL","config":{"prompt":"按终稿格式输出"}}
  ],
  "edges": [
    {"from":"parse","to":"search"},
    {"from":"search","to":"parallel"},
    {"from":"parallel","to":"merge"},
    {"from":"merge","to":"format_check"},
    {"from":"format_check","to":"completeness"},
    {"from":"completeness","to":"route"},
    {"from":"review","to":"approval"},
    {"from":"approval","to":"output"}
  ]
}
```

#### 步骤5：建设评估集

在 eval-service 积累评估数据：

- 收集20份已通过评审的真实申报书 → `eval_proposal_quality`
- 评估维度：章节完整性、格式合规率、关键要素覆盖率
- 每次优化Prompt后自动回归

---

### 9个场景建设路径汇总

| 场景 | 知识库 | Agent模式 | Workflow复杂度 | 关键Skill |
|------|--------|----------|---------------|-----------|
| 智能写作 | 历史申报书库、政策库 | Graph | 高（多章节并行） | 章节写作/格式校验/评审子Agent |
| 立项评审 | 历史评审案例库 | ReAct | 中（多维度串行） | 分项评分/评审意见生成 |
| 任务书评审 | 标准模板库 | ReAct | 低 | 规则比对/合规检测 |
| 任务管控 | 项目档案库 | Negotiation | 高（状态流转） | 阈值检测/预警通知 |
| 验收辅助 | 验收标准库 | ReAct | 中（多条目并行） | 逐项检查/报告生成 |
| 项目画像 | 项目全量数据 | ReAct | 中（并行分析） | 统计计算/标签生成 |
| 资料完整性 | 清单标准库 | ReAct | 低 | 文件检测/缺失匹配 |
| 经费合规 | 政策法规库 | ReAct | 低 | 规则匹配/异常标注 |
| 报表生成 | 多源业务数据 | Graph | 高（ETL流程） | 数据处理/图表生成 |

---

### 上层项目结构

上层9个场景独立项目 `AIApps`，与底座 `AIBase` 分离：

```
AIApps/
├── ai-apps-common/              # 场景间共享的通用Skill/工具
├── ai-apps-smart-writing/       # 1.智能写作（含5个模板配置）
│   ├── agents/                  # Agent定义JSON
│   ├── workflows/               # Workflow定义DSL
│   ├── skills/                  # Skill定义（Prompt模板 + 函数代码）
│   ├── eval-datasets/           # 评估数据集
│   └── frontend/                # 写作界面
├── ai-apps-project-review/      # 2.智能立项评审
├── ai-apps-task-review/         # 3.任务书辅助评审
├── ai-apps-task-control/        # 4.智能任务管控
├── ai-apps-acceptance/          # 5.智能验收辅助
├── ai-apps-project-profiling/   # 6.项目分类画像
├── ai-apps-completeness-check/  # 7.资料完整性检查
├── ai-apps-fund-compliance/     # 8.研发经费合规审查
└── ai-apps-report-generator/    # 9.智能报表生成
```

每个场景项目只包含：Agent定义配置、Workflow DSL、Skill定义、评估数据集、前端页面。通过底座 API 完成部署，不编写服务端代码。
