# AIBase - 通用 AI 底座

基于 Spring AI Alibaba 框架的企业级 AI 底座平台，集成知识库、Graph 工作流编排、多 Agent 协同、Skill 执行、MCP 协议支持等能力。

## 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway (8081)                          │
├──────────┬──────────┬──────────┬──────────┬────────────────┤
│ Knowledge │ Workflow │  Agent   │  Skill   │  MCP Gateway   │
│  (8101)  │  (8103)  │  (8105)  │  (8102)  │    (8106)      │
├──────────┼──────────┼──────────┼──────────┼────────────────┤
│ Model GW │   Eval   │ Platform │  Common  │    API         │
│  (8104)  │  (8107)  │  (8108)  │          │    (8080)      │
├──────────┴──────────┴──────────┴──────────┴────────────────┤
│  PostgreSQL / Redis / Milvus / RocketMQ / Nacos / MinIO     │
└─────────────────────────────────────────────────────────────┘
```

| 模块 | 端口 | 职责 |
|------|------|------|
| `ai-base-gateway` | 8081 | API 网关，鉴权，限流，CORS |
| `ai-base-api` | 8080 | 对外聚合 API |
| `ai-base-knowledge` | 8101 | 知识库管理，文档摄取，混合检索 |
| `ai-base-skill` | 8102 | 可复用技能执行 |
| `ai-base-workflow` | 8103 | DAG 工作流引擎 |
| `ai-base-model-gateway` | 8104 | 模型统一代理（LLM / Embedding） |
| `ai-base-agent` | 8105 | Agent 引擎（ReAct / Graph / Negotiation） |
| `ai-base-mcp-gateway` | 8106 | MCP 协议代理 |
| `ai-base-eval` | 8107 | 评估服务 |
| `ai-base-platform` | 8108 | 管理后台，审批，Prompt 管理 |
| `ai-base-common` | — | 共享 DTO、安全、工具类 |
| `ai-base-frontend` | 5173 | React + TypeScript 前端 |

## 技术栈

| 组件 | 选型 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot + Spring Cloud + Spring Cloud Alibaba | 3.3.0 / 2023.0.2 |
| 持久层 | MyBatis + Druid | 3.0.3 / 1.2.20 |
| 数据库 | PostgreSQL + pgvector | 16 |
| 向量数据库 | Milvus | 2.4.1 |
| 缓存 | Redis | 7 |
| 消息队列 | RocketMQ | 5.2.0 |
| 注册/配置中心 | Nacos | 2.3.2 |
| 对象存储 | MinIO | 2024 |
| 可观测性 | OpenTelemetry + Tempo + Loki + Grafana | — |
| 前端 | React + TypeScript + Vite + Zustand + Ant Design | — |

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- Node.js 18+
- Docker & Docker Compose

### 1. 启动基础设施

```bash
docker compose -f docker-compose/infrastructure.yml up -d
```

### 2. 初始化数据库

```bash
# 按顺序执行迁移脚本
for f in db/migrations/V*.sql; do
  psql -h localhost -U aibase -d aibase -f "$f"
done
```

### 3. 构建后端

```bash
mvn clean package -DskipTests
```

### 4. 启动后端服务

```bash
# 逐个启动或使用 Docker Compose
docker compose -f docker-compose/app.yml up -d
```

### 5. 启动前端

```bash
cd ai-base-frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 项目结构

```
AIBase/
├── ai-base-common/       # 共享模块
├── ai-base-api/          # API 聚合层
├── ai-base-gateway/      # 网关
├── ai-base-knowledge/    # 知识库服务
├── ai-base-skill/        # 技能服务
├── ai-base-workflow/     # 工作流引擎
├── ai-base-agent/        # Agent 引擎
├── ai-base-mcp-gateway/  # MCP 网关
├── ai-base-model-gateway/# 模型网关
├── ai-base-eval/         # 评估服务
├── ai-base-platform/     # 管理后台
├── ai-base-frontend/     # 前端
├── db/migrations/        # 数据库迁移脚本
├── docker-compose/       # Docker 编排配置
├── docs/                 # 文档
└── deploy.sh             # 部署脚本
```

## 部署

```bash
# 设置环境变量
export DEPLOY_SERVER=your-server-ip
export DEPLOY_USER=root
export DEPLOY_PASSWORD=your-password
export DASHSCOPE_API_KEY=your-dashscope-key

# 执行部署
bash deploy.sh
```

详见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

## 文档

- [部署指南](docs/DEPLOYMENT.md)
- [GraphRAG 可行性分析](docs/graphrag-analysis.md)
- [设计规格](docs/superpowers/specs/2026-06-01-ai-base-design.md)
- [AIBase vs Dify 功能对比](docs/dify-comparison.md)
