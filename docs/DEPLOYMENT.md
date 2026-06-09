# AIBase 部署文档

## 环境要求

| 组件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 21+ | 后端编译与运行 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 20+ | 前端构建 |
| Docker | 24+ | 容器化运行 |
| Docker Compose | 2.20+ | 编排部署 |

## 架构总览

```
                    ┌──────────────┐
                    │   Frontend   │  :80 (Nginx)
                    └──────┬───────┘
                           │ /api/*
                    ┌──────▼───────┐
                    │  API Gateway │  :8081 (Spring Cloud Gateway)
                    └──────┬───────┘
           ┌───────┬───────┼───────┬───────┬───────┬───────┐
           │       │       │       │       │       │       │
    ┌──────▼──┐ ┌──▼───┐ ┌─▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──────┐
    │Knowledge│ │Skill │ │WF  │ │Agent│ │MCP │ │Model│ │Eval/Plat│
    │  :8101  │ │:8102 │ │:8103│ │:8105│ │:8106│ │:8104│ │:8107/08 │
    └────┬────┘ └──┬───┘ └──┬─┘ └──┬──┘ └─────┘ └──┬──┘ └─────────┘
         │         │        │      │               │
         └─────────┴────────┴──────┴───────────────┘
                           │
    ┌──────────────────────┼──────────────────────────────┐
    │              基础设施层                                │
    │  PG:5432  Redis:6379  Nacos:8848  Milvus:19530      │
    │  MinIO:9000  RocketMQ:9876  Tempo:4317  Loki:3100   │
    └──────────────────────────────────────────────────────┘
```

## 服务端口一览

> **重要**：容器间通信使用 Docker Compose **服务名**（如 `knowledge`、`agent`），而非 `container_name`（如 `aibase-knowledge`）。Gateway 路由中的 `uri` 必须使用服务名，否则 DNS 解析失败。

| 服务 | 端口 | Docker 服务名 | 模块目录 |
|------|------|-------------|---------|
| frontend | 80 | `frontend` | `ai-base-frontend` |
| api-gateway | 8081 | `api-gateway` | `ai-base-gateway` |
| knowledge | 8101 | `knowledge` | `ai-base-knowledge` |
| skill | 8102 | `skill` | `ai-base-skill` |
| workflow | 8103 | `workflow` | `ai-base-workflow` |
| model-gateway | 8104 | `model-gateway` | `ai-base-model-gateway` |
| agent | 8105 | `agent` | `ai-base-agent` |
| mcp-gateway | 8106 | `mcp-gateway` | `ai-base-mcp-gateway` |
| eval | 8107 | `eval` | `ai-base-eval` |
| platform | 8108 | `platform` | `ai-base-platform` |

### 基础设施端口

| 组件 | 端口 | 用途 |
|------|------|------|
| PostgreSQL 16 + pgvector | 5432 | 业务数据 |
| Redis 7 | 6379 | 缓存 / Session |
| Nacos 2.3.2 | 8848, 9848 | 注册中心 / 配置中心 |
| etcd 3.5.5 | 2379 | Milvus 元数据 |
| MinIO | 9000, 9001 | 对象存储（Milvus 数据） |
| Milvus 2.4.1 | 19530, 9091 | 向量数据库（standalone 模式，auto-restart） |
| RocketMQ 5.2.0 | 9876, 10911 | 消息队列 |
| Grafana Tempo | 4317, 3200 | 链路追踪 |
| Grafana Loki | 3100 | 日志聚合 |
| Grafana | 3000 | 监控面板 |

## 快速部署

### 1. 配置环境变量

```bash
cd docker-compose
cp .env .env.local
```

编辑 `.env.local`，修改以下关键配置：

```bash
# 必填 — 至少配置一个模型 API Key
DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx   # 阿里云 DashScope
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx      # OpenAI 或兼容 API（DeepSeek/vLLM/Ollama 等）

# 必改 — 生产环境密码
PG_PASSWORD=your_secure_password
REDIS_PASSWORD=your_secure_password
MINIO_ROOT_PASSWORD=your_secure_password
GRAFANA_PASSWORD=your_secure_password

# 可选 — 按需调整 JVM 参数
JAVA_OPTS=-Xms256m -Xmx512m
```

### 2. 构建所有服务

**后端构建：**

```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

构建产物位于各模块 `target/` 目录下，如 `ai-base-agent/target/ai-base-agent-1.0.0-SNAPSHOT.jar`。

**前端构建（Docker 内自动完成，本地调试时手动执行）：**

```bash
cd ai-base-frontend
npm ci
npm run build
```

> 前端依赖 `@xyflow/react` v12 用于 Workflow DAG 画布编辑器，首次构建时需 `npm install`。

### 3. 初始化数据库

项目使用原始 SQL 迁移脚本管理表结构，位于 `db/migrations/` 目录。首次部署需要手动执行。

**方式一：Docker 初始化（推荐）**

```bash
# 直接把迁移目录挂载到 PostgreSQL 初始化目录
docker run --rm \
  --network aibase_default \
  -v "$(pwd)/db/migrations:/docker-entrypoint-initdb.d" \
  -e PGPASSWORD="${PG_PASSWORD:-aibase}" \
  postgres:16 \
  sh -c 'for f in /docker-entrypoint-initdb.d/V*.sql; do psql -h postgres -U aibase -d aibase -f "$f"; done'
```

**方式二：手动执行**

```bash
# 先启动基础设施
docker-compose up -d postgres

# 等待 PostgreSQL 就绪后，逐个执行迁移
for f in db/migrations/V*.sql; do
  docker-compose exec -T postgres \
    psql -U aibase -d aibase < "$f"
done
```

**方式三：psql 客户端直接执行**

如果本地安装了 `psql` 客户端：

```bash
for f in db/migrations/V*.sql; do
  PGPASSWORD=${PG_PASSWORD:-aibase} psql -h localhost -U aibase -d aibase -f "$f"
done
```

### 4. 启动服务

```bash
# 在项目根目录执行，使用根 docker-compose.yml 一键启动全部服务
# （包含 infrastructure + app + frontend，共享 aibase_default 网络）

# 方式一：一键启动全部（推荐）
docker-compose up -d

# 方式二：分步启动
# 先启动基础设施
docker-compose up -d postgres redis nacos etcd minio milvus rocketmq-namesrv rocketmq-broker tempo loki grafana

# 等待基础设施就绪（约 20-30 秒），再启动应用
docker-compose up -d knowledge skill workflow agent mcp-gateway model-gateway eval platform api-gateway

# 最后启动前端
docker-compose up -d frontend
```

> **注意**：
> - 分开启动 `infrastructure.yml` 和 `app.yml` 会创建**不同网络**，导致 Gateway 无法通过 Docker DNS 解析后端服务名。务必使用根 `docker-compose.yml`。
> - 服务器上使用 `docker-compose`（带连字符），而非 `docker compose`（插件模式在某些旧版 Docker 上行为不同）。

### 5. 初始化模型数据

服务启动后，需要在数据库中配置模型记录，模型网关才能正常工作：

```bash
# 连接 PostgreSQL 执行
docker-compose exec postgres psql -U aibase -d aibase
```

```sql
-- 阿里云 DashScope（默认模型）
INSERT INTO model_config (id, name, provider, api_key_ref, max_tokens, capabilities, priority, status, created_at, updated_at)
VALUES ('1730000000000000001', 'qwen-plus', 'DASHSCOPE', 'DASHSCOPE_API_KEY', 4096, 'chat,embed', 0, 'ACTIVE', NOW(), NOW());

-- OpenAI 官方（需要 OPENAI_API_KEY）
INSERT INTO model_config (id, name, provider, api_key_ref, max_tokens, capabilities, priority, status, created_at, updated_at)
VALUES ('1730000000000000002', 'gpt-4o', 'OPENAI', 'OPENAI_API_KEY', 4096, 'chat,embed', 1, 'ACTIVE', NOW(), NOW());
```

> 更多模型配置（DeepSeek、Ollama 等）见下方 [模型厂商配置](#模型厂商配置) 章节。

### 6. 验证部署

> **重要**：安全基础 (Phase 1) 已部署，所有 API 端点需要 `X-Api-Key` 头。默认开发 Key 为 `aibase-dev-key-2024`。

```bash
# 检查所有容器状态
docker-compose ps

# 检查是否有异常退出的容器
docker ps --filter 'name=aibase-' --filter 'status=exited'

# 通过网关测试各服务 API（使用正确的接口路径 + API Key）
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/knowledge/kb       # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/agent              # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/skill              # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/workflow           # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/model              # → {"success":true,"data":[...]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/mcp/servers        # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/eval/datasets      # → {"success":true,"data":[]}
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8081/api/v1/platform/prompts   # → {"success":true,"data":[]}

# 直连后端服务（排查网关问题时使用）
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8101/api/v1/knowledge/kb       # knowledge
curl -s -H "X-Api-Key: aibase-dev-key-2024" http://localhost:8105/api/v1/agent              # agent

# 打开前端页面
curl http://localhost
```

> **注意**：不要在根路径加 `/` 后缀（如 `/api/v1/knowledge/`），各服务 Controller 没有根路径的 `@GetMapping`，会导致 404 被 GlobalExceptionHandler 返回为 500（已修复：现返回 404 及 `"NOT_FOUND"`）。

## Docker 网络配置

所有服务必须在同一 Docker 网络 `aibase_default` 中，否则 Gateway 无法通过 Docker DNS 解析后端服务名。

### 关键规则

1. **从项目根目录启动**：Docker Compose 项目名取自目录名。从 `/u01/aibase/` 执行 `docker-compose up -d` 创建项目 `aibase` 和网络 `aibase_default`。从子目录（如 `/u01/aibase/docker-compose/`）执行会创建不同的项目名和隔离网络。
2. **app.yml 声明外部网络**：`app.yml` 中已配置 `networks.default.name: aibase_default` + `external: true`，确保应用服务加入已有网络。
3. **手动接入已有网络**：如果容器被错误创建在隔离网络中，使用 `docker network connect --alias <service-name> aibase_default <container>` 手动接入。

### 验证网络连通性

```bash
# 检查所有服务是否在 aibase_default 网络中
docker network inspect aibase_default --format '{{range $k, $v := .Containers}}{{$v.Name}} {{end}}'

# 从 Gateway 容器内检查能否解析后端服务名
docker exec aibase-gateway wget -qO- --timeout=2 http://knowledge:8101/ 2>&1 || echo "knowledge unreachable"
```

## 前端 API Key 配置

前端 `client.ts` 中的 axios 请求拦截器已配置 `X-Api-Key` 头，默认值 `aibase-dev-key-2024`。可通过浏览器的 localStorage 覆盖：

```javascript
// 自定义 API Key（开发/测试时在浏览器 Console 执行）
localStorage.setItem('apiKey', 'your-custom-key');

// 自定义用户/部门标识
localStorage.setItem('userId', 'your-user-id');
localStorage.setItem('deptId', 'your-dept-id');
```

不携带 `X-Api-Key` 的请求将被后端 `ApiKeyAuthFilter` 拦截并返回 401。

## 常用运维命令

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f agent
docker-compose logs -f model-gateway

# 重启单个服务
docker-compose restart agent

# 停止所有服务
docker-compose down

# 停止并清理数据卷（重置数据库）
docker-compose down -v

# 只重建某个服务（代码变更后）
docker-compose up -d --build agent

# 查看资源使用
docker stats --no-stream --filter 'name=aibase-'
```

## 本地开发调试

开发时无需 Docker，直接启动所需基础设施，然后 IDE 中运行各服务。

### 1. 启动基础设施

```bash
docker-compose up -d postgres redis nacos etcd minio milvus rocketmq-namesrv rocketmq-broker
```

### 2. 配置 IDE 启动参数

各服务 `application.yml` 中的 `server.port` 已配置好：
- knowledge: 8101
- skill: 8102
- workflow: 8103
- model-gateway: 8104
- agent: 8105
- mcp-gateway: 8106
- eval: 8107
- platform: 8108
- api-gateway: 8081

前端开发：

```bash
cd ai-base-frontend
npm run dev
```

### 3. 跨服务调用地址

本地开发时，各服务通过以下默认地址相互调用（可通过环境变量覆盖）：

| 调用方 | 目标服务 | 默认地址 |
|--------|---------|---------|
| agent | model-gateway | `http://localhost:8104` |
| knowledge | model-gateway | `http://localhost:8104` |
| workflow | skill | `http://localhost:8102` |
| workflow | agent | `http://localhost:8105` |

Docker 部署时通过环境变量覆盖为容器名（已在 `app.yml` 的 `x-java-defaults` 中统一配置）。

## 模型厂商配置

模型网关通过可插拔的 Provider 架构支持多种大模型厂商，系统默认自带两个 Provider：

| Provider | 厂商 | 协议 | 适用模型 |
|----------|------|------|---------|
| `DASHSCOPE` | 阿里云 DashScope | DashScope 原生 | 通义千问系列、text-embedding-v3 |
| `OPENAI` | OpenAI 官方 / 兼容 API | OpenAI-compatible | GPT-4o、DeepSeek、GLM、vLLM、Ollama 等 |

### 添加模型配置

通过数据库 `model_config` 表注册模型，系统根据 `provider` 字段自动路由到对应 Provider：

```sql
-- 阿里云 DashScope
INSERT INTO model_config (id, name, provider, endpoint, api_key_ref, max_tokens, capabilities, priority, status)
VALUES (1730000000000000001, 'qwen-plus', 'DASHSCOPE', NULL, 'DASHSCOPE_API_KEY', 4096, 'chat,embed', 0, 'ACTIVE');

-- OpenAI 官方
INSERT INTO model_config (id, name, provider, endpoint, api_key_ref, max_tokens, capabilities, priority, status)
VALUES (1730000000000000002, 'gpt-4o', 'OPENAI', NULL, 'OPENAI_API_KEY', 4096, 'chat,embed', 1, 'ACTIVE');

-- DeepSeek（OpenAI 兼容协议）
INSERT INTO model_config (id, name, provider, endpoint, api_key_ref, max_tokens, capabilities, priority, status)
VALUES (1730000000000000003, 'deepseek-chat', 'OPENAI', 'https://api.deepseek.com', 'OPENAI_API_KEY', 4096, 'chat', 2, 'ACTIVE');

-- Ollama 本地部署（OpenAI 兼容协议）
INSERT INTO model_config (id, name, provider, endpoint, api_key_ref, max_tokens, capabilities, priority, status)
VALUES (1730000000000000004, 'llama3:8b', 'OPENAI', 'http://host.docker.internal:11434', 'OPENAI_API_KEY', 2048, 'chat,embed', 3, 'ACTIVE');
```

- `endpoint` 为空时使用 Provider 默认地址；设置为兼容 API 地址时，OpenAIProvider 会自动拼接 `/v1/chat/completions` 和 `/v1/embeddings`
- `api_key_ref` 为环境变量名，Provider 会从 `System.getenv()` 读取
- `priority=0` 为默认模型，路由规则未匹配时使用

### 扩展新厂商

新增模型厂商只需实现 `ModelProvider` 接口并注册为 Spring `@Component`，无需修改现有代码：

```java
@Component
public class AzureProvider implements ModelProvider {
    @Override
    public String getProviderName() { return "AZURE"; }

    @Override
    public Map<String, Object> chat(String endpoint, String apiKey, String model, Map<String, Object> request) {
        // 对接 Azure OpenAI API
    }

    @Override
    public Map<String, Object> embed(String endpoint, String apiKey, String model, String text) {
        // 对接 Azure OpenAI Embedding API
    }
}
```

然后在 `model_config` 表中使用 `provider='AZURE'` 即可。

## 环境变量参考

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | JVM 启动参数 |
| `PG_HOST` | `localhost` | PostgreSQL 主机 |
| `PG_PORT` | `5432` | PostgreSQL 端口 |
| `PG_DB` | `aibase` | 数据库名 |
| `PG_USER` | `aibase` | 数据库用户 |
| `PG_PASSWORD` | `aibase` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | — | Redis 密码 |
| `NACOS_SERVER` | `localhost:8848` | Nacos 服务地址 |
| `MILVUS_HOST` | `localhost` | Milvus 主机 |
| `MILVUS_PORT` | `19530` | Milvus 端口 |
| `DASHSCOPE_API_KEY` | — | 阿里云 DashScope API Key |
| `OPENAI_API_KEY` | — | OpenAI 或兼容 API Key |
| `EMBEDDING_MODEL` | `text-embedding-v3` | 嵌入模型名称 |
| `FRONTEND_PORT` | `80` | 前端对外端口 |

## 监控与可观测性

- **Grafana**: http://localhost:3000（默认账号 admin，密码见 `.env`）
- **Tempo**（链路追踪）: http://localhost:3200
- **MinIO Console**: http://localhost:9001
- **Nacos Console**: http://localhost:8848/nacos
- **应用 Metrics**: `http://localhost:<port>/actuator/prometheus`

## 扩容建议

当前为单节点部署，生产环境建议：

1. **PostgreSQL**: 主从复制 + 定期备份
2. **Redis**: 哨兵模式或 Cluster
3. **Nacos**: 3 节点集群
4. **MinIO**: 多节点纠删码模式
5. **Milvus**: 读写分离 + 多副本
6. **应用服务**: 每个服务至少 2 副本，配合 Nacos 负载均衡
7. **前端**: Nginx 前加 CDN / 负载均衡器

## Nacos 配置说明

### 当前架构

- **Nacos Config**（HTTP 8848）：已集成，各服务通过 `spring.config.import` 从 Nacos 拉取配置
- **Nacos Discovery**（gRPC 9848）：**已禁用**，原因见下

### Nacos Discovery 禁用原因

Nacos 2.x 客户端通过 gRPC（端口 9848）与服务端通信进行服务注册/发现。在 Docker 网络环境下，存在以下问题：

1. Nacos 服务端通过内网 IP 向客户端通告 gRPC 地址，Docker 容器间无法直接访问该 IP
2. 配置 `PREFER_HOST_MODE=hostname` 可修复服务端地址通告，但客户端初始连接仍可能指向 `127.0.0.1:9848`

当前方案：Gateway 使用静态 URI `http://<docker-service-name>:port` 路由，依赖 Docker DNS 做服务发现。如需启用 Nacos Discovery，需要额外配置 Nacos 客户端的 gRPC 连接地址。

### Nacos 配置管理

各服务启动时会尝试从 Nacos 拉取以下 dataId 的配置：
- `ai-base.yml`（公共配置）
- `<spring.application.name>.yml`（服务专属配置，如 `ai-base-knowledge.yml`）

配置使用 `optional:` 前缀，即使 Nacos 中尚无配置数据，服务也可正常启动。首次使用时 Nacos 中配置为空属正常现象。

## 常见问题排查

### 1. Gateway 返回 500，日志显示 `UnknownHostException`

Gateway 无法通过 Docker DNS 解析后端服务名。检查：
- 是否使用根 `docker-compose.yml` 启动（确保所有容器在同一 `aibase_default` 网络）
- Gateway `application.yml` 中的 `uri` 是否为 Docker 服务名（如 `knowledge`），而非 `container_name`（如 `aibase-knowledge`）

### 2. API 返回 `{"success":false,"errorCode":"UNKNOWN","errorMsg":"Internal server error"}`

通常是请求路径不正确，`NoResourceFoundException` 被 GlobalExceptionHandler 捕获。检查：
- 请求路径是否包含具体方法路径（如 `/api/v1/knowledge/kb` 而非 `/api/v1/knowledge/`）
- 更新后的版本已将此类错误正确返回为 404

### 3. Milvus 容器反复退出

**症状**：Milvus 容器持续重启（`Up 7 seconds` → restart loop），日志显示：
- `failed to check blob bucket exist: The request signature we calculated does not match`
- `find no available datacoord, check datacoord state`
- `find no available querycoord, check querycoord state`

**根因**：`.env` 中 `MINIO_ROOT_PASSWORD` 与 Milvus 默认 MinIO 凭据不一致。`.env` 设定了自定义密码（如 `minioadmin_change_me`），但 Milvus 容器未配置 `MINIO_ACCESS_KEY_ID` / `MINIO_SECRET_ACCESS_KEY` 环境变量，默认使用 `minioadmin/minioadmin` 连接 MinIO，导致签名验证失败，无法创建 blob bucket，内部组件（datacoord/querycoord）无法初始化，容器崩溃。

**修复**：在 `docker-compose/infrastructure.yml` 的 Milvus 服务中显式配置 MinIO 凭据环境变量，与 `.env` 保持一致：

```yaml
milvus:
  environment:
    ETCD_ENDPOINTS: etcd:2379
    MINIO_ADDRESS: minio:9000
    MINIO_ACCESS_KEY_ID: minioadmin
    MINIO_SECRET_ACCESS_KEY: ${MINIO_ROOT_PASSWORD:-minioadmin}
```

**注意**：修改凭据后需清理 etcd 中旧 Milvus 实例的注册数据（`etcdctl del --prefix /by-dev`），否则新实例与旧组件 ID 不匹配仍会报 `node not match` 错误。

### 4. Nacos 日志刷屏 `Client not connected, current status:STARTING`

无害。Nacos Config 客户端尝试通过 gRPC 监听配置变更，但 gRPC 连接未建立。配置拉取（HTTP）正常工作，服务功能不受影响。

### 5. 后端服务返回 500，直连也报错

排查步骤：
```bash
# 1. 查看服务日志
docker logs aibase-knowledge --tail 100

# 2. 确认 PostgreSQL 可连通
docker exec aibase-knowledge wget -qO- http://postgres:5432/ 2>&1 || echo "PG unreachable"

# 3. 检查数据库表是否存在
docker exec aibase-pg psql -U aibase -d aibase -c '\dt'

# 4. 检查 JVM 内存是否充足
docker stats --no-stream --filter 'name=aibase-'
```

### 6. MCP Gateway 启动失败 — MyBatis BindingException

**症状**：MCP Gateway 启动时报 `BindingException: Invalid bound statement (not found): com.datang.aibase.mcpgateway.server.ExportableTool.getName`

**原因**：`@MapperScan` 包路径过宽，MyBatis 将 `ExportableTool` 接口误认为 Mapper 并创建代理，调用非 Mapper 方法时找不到 XML 映射语句。

**修复**（已在代码中修复）：
1. `McpGatewayApplication.java` — `@MapperScan` 精确限定为 `{"com.datang.aibase.mcp.mapper", "com.datang.aibase.mcpgateway.mapper"}`
2. `LocalToolRegistry.java` — 构造函数参数从 `List<ExportableTool>` 改为 `ObjectProvider<ExportableTool>`，避免 Spring 将 ExportableTool Bean 暴露给 MyBatis

> **教训**：`@MapperScan` 只应扫描真实的 MyBatis Mapper 接口所在包，不要扫描顶层 `"com.datang.aibase"` 或包含非 Mapper 接口的包。

### 7. MCP Gateway 连接不上 PostgreSQL

**症状**：首次 API 调用时 `CannotGetJdbcConnectionException: Failed to obtain JDBC Connection`，HikariPool 懒加载失败。

**原因**：MCP Gateway 容器可能被创建在错误的 Docker 网络（如 `docker-compose_default`），无法解析 `postgres` 主机名。

**修复**：
```bash
# 将 MCP Gateway 接入 aibase_default 网络
docker network disconnect docker-compose_default aibase-mcp-gateway 2>/dev/null
docker network connect --alias mcp-gateway aibase_default aibase-mcp-gateway

# 验证
docker exec aibase-mcp-gateway wget -qO- --timeout=2 http://postgres:5432/ 2>&1
```
永久修复已在 `app.yml` 中通过 `networks.default.external: true` 配置。

## Dockerfile 说明

每个后端服务使用相同的 Dockerfile 模式：

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/<module>-*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
```

前端使用多阶段构建（Dockerfile 在 `ai-base-frontend/Dockerfile`）：
- Stage 1: Node 20 Alpine 编译 React 应用
- Stage 2: Nginx Alpine 提供静态文件 + API 反向代理
