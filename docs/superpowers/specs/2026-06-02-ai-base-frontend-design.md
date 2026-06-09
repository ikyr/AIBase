# AIBase 前端管理控制台设计规格

## 概述

基于 AIBase 后端底座（Spring Boot 微服务，8 个业务模块 + API Gateway），建设统一管理控制台前端。控制台同时面向**系统管理员**（知识库配置、Agent 定义、Skill 注册、MCP/模型管理等平台管理）和**业务用户**（Agent 对话、知识库检索、工作流执行等 AI 能力使用）。

**设计理念**：AI 优先，首页即 Agent 对话界面。管理功能通过顶部导航按需访问。

## 技术栈

| 组件 | 选型 | 版本 |
|------|------|------|
| 框架 | React | 18.x |
| 语言 | TypeScript | 5.x |
| 构建 | Vite | 5.x |
| UI 组件库 | Ant Design | 5.x |
| 路由 | React Router | 6.x |
| 状态管理 | Zustand | 4.x |
| HTTP 客户端 | Axios | 1.x |
| 日期处理 | dayjs | 1.x |

**后端对接**：通过 Vite 开发代理 `/api` → `http://localhost:8081`，调用 AIBase API Gateway 的 8 条路由。

## 架构：模块化 SPA

单个 React 应用，按后端模块拆分为 9 个懒加载路由组（8 个业务模块 + 1 个对话首页），共享一套布局壳、用户态、API 客户端、设计系统。

```
ai-base-frontend/
├── public/
├── src/
│   ├── layouts/
│   │   ├── ConsoleLayout.tsx      # 顶部导航壳（8模块切换 + 用户头像）
│   │   └── ModuleLayout.tsx       # 左侧子菜单 + 右侧内容区
│   ├── modules/
│   │   ├── chat/                  # Agent 对话（首页 /）
│   │   │   ├── pages/
│   │   │   │   └── ChatPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── SessionList.tsx     # 左侧会话历史
│   │   │   │   ├── ChatArea.tsx        # 对话消息区
│   │   │   │   ├── MessageBubble.tsx   # 消息气泡
│   │   │   │   └── ChatInput.tsx       # 底部输入栏
│   │   │   └── stores/
│   │   │       └── chatStore.ts
│   │   ├── workflow/              # 工作流
│   │   │   ├── pages/
│   │   │   │   ├── WorkflowListPage.tsx
│   │   │   │   ├── WorkflowEditorPage.tsx
│   │   │   │   └── WorkflowInstancePage.tsx
│   │   │   ├── components/
│   │   │   │   ├── DAGCanvas.tsx        # DAG 画布（React Flow）
│   │   │   │   └── NodeConfigPanel.tsx
│   │   │   └── stores/
│   │   │       └── workflowStore.ts
│   │   ├── knowledge/             # 知识库
│   │   │   ├── pages/
│   │   │   │   ├── KbListPage.tsx
│   │   │   │   ├── KbDetailPage.tsx
│   │   │   │   └── DocumentUploadPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── KbCreateModal.tsx
│   │   │   │   ├── ConnectorConfigCard.tsx
│   │   │   │   └── SearchEngineConfigCard.tsx
│   │   │   └── stores/
│   │   │       └── knowledgeStore.ts
│   │   ├── agent/                 # Agent 管理
│   │   │   ├── pages/
│   │   │   │   ├── AgentListPage.tsx
│   │   │   │   ├── AgentDetailPage.tsx
│   │   │   │   └── SessionHistoryPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── AgentFormModal.tsx
│   │   │   │   └── AgentStatsCard.tsx
│   │   │   └── stores/
│   │   │       └── agentStore.ts
│   │   ├── skill/                 # Skill 管理
│   │   │   ├── pages/
│   │   │   │   ├── SkillListPage.tsx
│   │   │   │   ├── SkillDetailPage.tsx
│   │   │   │   └── SkillVersionHistory.tsx
│   │   │   ├── components/
│   │   │   │   ├── SkillFormModal.tsx
│   │   │   │   └── SkillExecutionLog.tsx
│   │   │   └── stores/
│   │   │       └── skillStore.ts
│   │   ├── mcp/                   # MCP 管理
│   │   │   ├── pages/
│   │   │   │   ├── McpServerListPage.tsx
│   │   │   │   ├── McpServerDetailPage.tsx
│   │   │   │   └── McpToolListPage.tsx
│   │   │   ├── components/
│   │   │   │   └── McpServerFormModal.tsx
│   │   │   └── stores/
│   │   │       └── mcpStore.ts
│   │   ├── model/                 # 模型管理
│   │   │   ├── pages/
│   │   │   │   ├── ModelListPage.tsx
│   │   │   │   ├── ModelRouteRulePage.tsx
│   │   │   │   └── ModelCallLogPage.tsx
│   │   │   ├── components/
│   │   │   │   └── ModelFormModal.tsx
│   │   │   └── stores/
│   │   │       └── modelStore.ts
│   │   ├── eval/                  # 评估
│   │   │   ├── pages/
│   │   │   │   ├── EvalDatasetListPage.tsx
│   │   │   │   ├── EvalTaskListPage.tsx
│   │   │   │   └── EvalResultPage.tsx
│   │   │   ├── components/
│   │   │   │   └── AnnotationPanel.tsx
│   │   │   └── stores/
│   │   │       └── evalStore.ts
│   │   └── platform/              # 平台管理
│   │       ├── pages/
│   │       │   ├── PromptVersionPage.tsx
│   │       │   └── ApprovalPage.tsx
│   │       ├── components/
│   │       │   └── PromptDiffView.tsx
│   │       └── stores/
│   │           └── platformStore.ts
│   ├── shared/
│   │   ├── api/
│   │   │   ├── client.ts          # Axios 实例（baseURL, 拦截器, X-User-Id/X-Dept-Id）
│   │   │   ├── types.ts           # ApiResponse<T>, 分页类型
│   │   │   ├── knowledge.ts       # /api/v1/knowledge/*
│   │   │   ├── workflow.ts        # /api/v1/workflow/*
│   │   │   ├── agent.ts           # /api/v1/agent/*
│   │   │   ├── skill.ts           # /api/v1/skill/*
│   │   │   ├── mcp.ts             # /api/v1/mcp/*
│   │   │   ├── model.ts           # /api/v1/model/*
│   │   │   ├── eval.ts            # /api/v1/eval/*
│   │   │   └── platform.ts        # /api/v1/platform/*
│   │   ├── components/
│   │   │   ├── StatCard.tsx        # 统计卡片（大数字+标签）
│   │   │   ├── StatusTag.tsx       # 状态标签（ACTIVE/PENDING/FAILED等）
│   │   │   ├── SearchBar.tsx       # 搜索输入框
│   │   │   ├── EmptyState.tsx      # 空状态占位
│   │   │   └── PageHeader.tsx      # 页面标题+操作按钮
│   │   ├── hooks/
│   │   │   ├── useRequest.ts      # 通用请求 Hook（loading/error/data）
│   │   │   └── usePagination.ts   # 分页 Hook
│   │   └── theme/
│   │       └── tokens.ts          # Ant Design 5 theme token 覆盖
│   ├── router.tsx                 # createBrowserRouter 集中路由配置
│   ├── App.tsx
│   └── main.tsx
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts
```

**文件组织原则**：
- 每个模块独立目录，包含 pages / components / stores
- 共享组件统一放 `shared/components/`
- API 客户端按模块分文件，共用 Axios 实例
- Zustand store 按模块独立，不设全局 store

## 布局结构

### 整体布局（ConsoleLayout）

```
┌─────────────────────────────────────────────────────────┐
│  AIBase  💬对话 ⚙️工作流 📚知识库 │ 🤖Agent 🛠Skill ...  👤 │  ← 48px 顶栏
├─────────────────────────────────────────────────────────┤
│ ┌──────────┐  ┌──────────────────────────────────────┐  │
│ │ 子菜单    │  │                                      │  │
│ │          │  │        内容区                          │  │
│ │ · 列表   │  │                                      │  │
│ │ · 详情   │  │                                      │  │
│ │          │  │                                      │  │
│ └──────────┘  └──────────────────────────────────────┘  │
│   200px                     flex-1                       │
└─────────────────────────────────────────────────────────┘
```

**顶栏规则**：
- 左侧：Logo（AIBase，主色 `#597ef7` 高亮 "AI"）
- 业务模块（对话/工作流/知识库）左对齐
- 竖线分隔 `│`
- 管理模块（Agent/Skill/MCP/模型/评估）紧随其后
- 右侧：用户头像（圆角方形 28x28，8px 圆角）

**侧边栏规则**：
- 宽度 200px，背景 `#fafafa`
- 子菜单项 13px，8px 圆角，激活态白色背景 + 微投影
- 分组标题 11px 大写灰色

### 对话页面布局（ChatPage，首页 /）

```
┌─────────────────────────────────────────────────────────┐
│  顶栏（对话高亮）                                         │
├────────────┬────────────────────────────────────────────┤
│ 会话历史    │  消息区                                    │
│ (200px)    │  ┌──────────────────────────────┐         │
│            │  │ 🤖 你好，我是AI助手...        │         │
│ · 会话1    │  ├──────────────────────────────┤         │
│ · 会话2    │  │ 👤 帮我写申报书...            │         │
│ · 会话3    │  ├──────────────────────────────┤         │
│            │  │ 🤖 好的，已检索3份案例...      │         │
│            │  └──────────────────────────────┘         │
│ + 新建会话  │  ┌──────────────────────────────┐         │
│            │  │ [输入框_________________] [发送]│         │
└────────────┴────────────────────────────────────────────┘
```

**详情**：
- 左侧 200px 会话列表（按时间倒序，标题截断）
- 消息气泡：用户蓝底右对齐，AI 灰底左对齐，12px 圆角
- 输入栏固定在底部，10px 圆角输入框 + 主色发送按钮
- 支持 Enter 发送，Shift+Enter 换行

### 管理页面布局（ModuleLayout）

```
┌─────────────────────────────────────────────────────────┐
│  顶栏（对应模块高亮）                                      │
├────────────┬────────────────────────────────────────────┤
│ 子菜单      │  ┌─────┐ ┌─────┐ ┌─────┐                 │
│            │  │ 12  │ │  8  │ │1247 │  ← 统计卡片行     │
│ · 列表     │  │Agent│ │运行中│ │今日 │                 │
│ · 详情     │  └─────┘ └─────┘ └─────┘                 │
│            │                                            │
│            │  ┌────────────────────────────┐            │
│            │  │ 申报书写作Agent    ACTIVE   │ ← 卡片列表  │
│            │  │ qwen-max · Graph · 4 Skill │            │
│            │  └────────────────────────────┘            │
│            │  ┌────────────────────────────┐            │
│            │  │ 立项评审Agent      ACTIVE   │            │
│            │  └────────────────────────────┘            │
└────────────┴────────────────────────────────────────────┘
```

**详情**：
- 顶部统计卡片行：浅灰底 `#fafafa`，12px 圆角，大数字 22px/700
- 列表卡片：白底 12px 圆角，1px `#f0f0f0` 边框，hover 微投影 `0 2px 8px rgba(0,0,0,0.04)`
- 卡片内：标题 14px/600 + 元信息 12px `#999` + 右侧状态标签

## 路由树

| 路径 | 页面 | 模块 |
|------|------|------|
| `/` | ChatPage（Agent 对话） | chat |
| `/workflow` | WorkflowListPage | workflow |
| `/workflow/:id` | WorkflowEditorPage（DAG 画布） | workflow |
| `/workflow/instance/:id` | WorkflowInstancePage（执行监控） | workflow |
| `/knowledge` | KbListPage | knowledge |
| `/knowledge/:id` | KbDetailPage | knowledge |
| `/knowledge/:id/upload` | DocumentUploadPage | knowledge |
| `/agent` | AgentListPage | agent |
| `/agent/:id` | AgentDetailPage | agent |
| `/agent/sessions` | SessionHistoryPage | agent |
| `/skill` | SkillListPage | skill |
| `/skill/:id` | SkillDetailPage | skill |
| `/skill/:id/versions` | SkillVersionHistory | skill |
| `/mcp` | McpServerListPage | mcp |
| `/mcp/:id` | McpServerDetailPage | mcp |
| `/mcp/tools` | McpToolListPage | mcp |
| `/model` | ModelListPage | model |
| `/model/routes` | ModelRouteRulePage | model |
| `/model/logs` | ModelCallLogPage | model |
| `/eval` | EvalDatasetListPage | eval |
| `/eval/tasks` | EvalTaskListPage | eval |
| `/eval/results/:id` | EvalResultPage | eval |
| `/platform/prompts` | PromptVersionPage | platform |
| `/platform/approvals` | ApprovalPage | platform |

总计 **24 个路由**，全部通过 `React.lazy()` + `Suspense` 懒加载。

## 设计系统

### 色彩

| 令牌 | 色值 | 用途 |
|------|------|------|
| `--primary` | `#597ef7` | 主操作按钮、激活态、链接 |
| `--primary-hover` | `#85a5ff` | hover 状态 |
| `--primary-bg` | `#f0f5ff` | 选中背景、标签底色 |
| `--surface` | `#fff` | 卡片、内容区背景 |
| `--bg-base` | `#fafafa` | 侧边栏背景 |
| `--bg-hover` | `#f5f5f5` | 列表项 hover |
| `--border` | `#f0f0f0` | 卡片边框、分割线 |
| `--text-primary` | `#1a1a1a` | 标题文字 |
| `--text-secondary` | `#555` | 正文 |
| `--text-tertiary` | `#999` | 辅助说明、占位符 |
| `--success` | `#16a34a` | SUCCESS 状态 |
| `--warning` | `#d97706` | PENDING 状态 |
| `--error` | `#eb5757` | FAILED/删除操作 |

### 字体层级

| 层级 | 字号 | 字重 | 用途 |
|------|------|------|------|
| h2 | 20px | 700 | 页面标题 |
| h3 | 16px | 600 | 区块标题 |
| card-title | 14px | 600 | 卡片标题 |
| body | 13px | 400 | 正文，行高 1.5 |
| caption | 12px | 400 | 辅助说明、时间、计数 |
| label | 11px | 600 | 大写标签，letter-spacing 0.5px |

**字体栈**：`-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`

### 圆角体系

| 元素 | 圆角 | 
|------|------|
| 标签 (Tag) | 6px |
| 按钮 (Button) | 8px |
| 输入框 (Input) | 10px |
| 卡片 (Card) | 12px |
| 模态框 (Modal) | 16px |

### 间距（4px 基准）

| 用途 | 值 |
|------|-----|
| 组件内间距 | 8-12px |
| 卡片间距 | 14-16px |
| 区块间距 | 20-24px |
| 页面内边距 | 20px |
| 顶栏高度 | 48px |
| 侧边栏宽度 | 200px |

### 投影

- 卡片默认：无投影（仅 1px 边框）
- 卡片 hover：`0 2px 8px rgba(0,0,0,0.04)`
- 模态框：`0 4px 16px rgba(0,0,0,0.08)`
- 侧边栏激活项：`0 1px 3px rgba(0,0,0,0.04)`

### 关键组件模式

**按钮**：10px 圆角，主操作 `#597ef7` 白字，次要 `#f5f5f5` 深字，危险 `#fff0f0` + `#eb5757`

**状态标签**：6px 圆角，11px/500 字重。ACTIVE→蓝底蓝字，SUCCESS→绿底绿字，PENDING→琥珀底琥珀字，FAILED→红底红字

**卡片**：12px 圆角，白底 `#fff`，1px `#f0f0f0` 边框。hover 时出现微投影。内边距 16-18px。

**输入框**：10px 圆角，36px 高，1px `#e0e0e0` 边框。focus 时边框变 `#597ef7` + `0 0 0 2px rgba(89,126,247,0.1)` 光环。

**统计卡片**：`#fafafa` 底，12px 圆角，大数字 22px/700，标签 12px `#888`

## API 层

### Axios 客户端

```typescript
// shared/api/client.ts
const client = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
});

client.interceptors.request.use((config) => {
  config.headers['X-User-Id'] = getUserId();    // 从 Zustand 或 localStorage
  config.headers['X-Dept-Id'] = getDeptId();
  return config;
});

client.interceptors.response.use(
  (res) => res.data,           // 解包 ApiResponse
  (err) => { /* 统一错误处理 */ }
);
```

### 后端 API 映射

| 前端 API 文件 | 后端路由前缀 | 主要端点 |
|--------------|-------------|---------|
| `knowledge.ts` | `/api/v1/knowledge` | CRUD kb, search, ingest, stats |
| `workflow.ts` | `/api/v1/workflow` | execute, getInstance, cancel |
| `agent.ts` | `/api/v1/agent` | chat, getSession |
| `skill.ts` | `/api/v1/skill` | CRUD skill（placeholder） |
| `mcp.ts` | `/api/v1/mcp` | CRUD server/tool（placeholder） |
| `model.ts` | `/api/v1/model` | CRUD model/route/log（placeholder） |
| `eval.ts` | `/api/v1/eval` | CRUD dataset/task/result（placeholder） |
| `platform.ts` | `/api/v1/platform` | prompt versions, approvals（placeholder） |

响应格式统一为 `ApiResponse<T>`：
```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: string | null;
}
```

**注意**：当前仅 knowledge 模块后端 Service 有真实实现，其余模块为 placeholder（返回模拟数据）。前端 API 层按完整接口编写，后续后端完善时无需改动前端。

## 状态管理（Zustand）

每个模块一个独立 store，不设全局 store。需要跨模块共享的状态（如用户信息）通过 `shared/stores/userStore.ts` 管理。

```typescript
// 示例：chatStore.ts
interface ChatState {
  sessions: Session[];
  activeSessionId: string | null;
  messages: Message[];
  loading: boolean;
  sendMessage: (content: string) => Promise<void>;
  createSession: () => void;
  switchSession: (id: string) => void;
}
```

**Store 规范**：
- 每个 store 文件 < 100 行
- 异步操作在 store 内通过 `client.ts` Axios 实例处理
- 请求状态用 `loading/error` 字段，页面组件据此渲染加载态/错误态

## 模块页面详情

### chat（首页）

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| ChatPage | `/` | 会话列表 + 消息区 + 输入栏 |

**交互状态**：
- 消息发送中：消息气泡右侧显示 loading 动画
- AI 回复中：显示逐字输出动画（模拟打字效果）
- 错误：红色提示 "发送失败，点击重试"
- 空状态：新用户无会话时，显示 "开始一段新对话" 引导

### workflow

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| WorkflowListPage | `/workflow` | 工作流定义列表 + 新建/删除 |
| WorkflowEditorPage | `/workflow/:id` | DAG 画布编辑（React Flow） |
| WorkflowInstancePage | `/workflow/instance/:id` | 执行状态监控 + 节点日志 |

### knowledge

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| KbListPage | `/knowledge` | 知识库卡片网格 + 新建弹窗 |
| KbDetailPage | `/knowledge/:id` | 连接器/搜索引擎配置 + 文档列表 |
| DocumentUploadPage | `/knowledge/:id/upload` | 文件拖拽上传 + 摄取进度 |

### agent

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| AgentListPage | `/agent` | Agent 卡片列表 + 统计 + 新建 |
| AgentDetailPage | `/agent/:id` | Agent 配置详情（model/prompt/skills/kbs） |
| SessionHistoryPage | `/agent/sessions` | 会话记录列表 + 搜索 |

### skill

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| SkillListPage | `/skill` | Skill 卡片列表（按 Level 分组） |
| SkillDetailPage | `/skill/:id` | Skill 定义详情 + 参数 Schema |
| SkillVersionHistory | `/skill/:id/versions` | 版本时间线 + Diff 对比 |

### mcp

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| McpServerListPage | `/mcp` | MCP Server 列表 + 健康状态 |
| McpServerDetailPage | `/mcp/:id` | Server 详情 + 连接日志 |
| McpToolListPage | `/mcp/tools` | 所有注册工具列表 + 搜索 |

### model

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| ModelListPage | `/model` | 模型配置列表 + 优先级排序 |
| ModelRouteRulePage | `/model/routes` | 路由规则列表 + 条件编辑 |
| ModelCallLogPage | `/model/logs` | 调用日志表格 + 成本统计 |

### eval

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| EvalDatasetListPage | `/eval` | 评估数据集列表 + 新建 |
| EvalTaskListPage | `/eval/tasks` | 评估任务列表 + 执行结果 |
| EvalResultPage | `/eval/results/:id` | 单次评估详细结果 + 标注面板 |

### platform

| 页面 | 路由 | 核心功能 |
|------|------|---------|
| PromptVersionPage | `/platform/prompts` | Prompt 版本列表 + 发布/回滚 |
| ApprovalPage | `/platform/approvals` | 审批任务列表 + 审批操作 |

### 通用交互状态

每个页面/组件覆盖以下状态：
- **Loading**：骨架屏（Ant Design Skeleton）或 Spin
- **Empty**：EmptyState 组件（图标 + 引导文案 + 操作按钮）
- **Error**：Alert 组件（错误信息 + 重试按钮）
- **Success**：正常数据渲染

## 实现顺序

按依赖关系和用户可见价值排序：

1. **项目脚手架** — Vite + React + TypeScript + Ant Design 安装配置，主题 token 覆盖
2. **布局壳** — ConsoleLayout（顶栏 + 侧边栏）+ 路由框架 + 懒加载
3. **共享组件** — StatCard, StatusTag, SearchBar, EmptyState, PageHeader + API 客户端
4. **Chat 模块** — 首页对话界面（最高用户价值，最早上线可见）
5. **Knowledge 模块** — 知识库 CRUD（后端已有真实实现，端到端验证）
6. **Agent 模块** — Agent 列表/详情（配置管理核心）
7. **Workflow 模块** — 列表 + DAG 编辑器
8. **Skill 模块** — 列表 + 版本历史
9. **MCP + Model 模块** — 基础设施管理
10. **Eval + Platform 模块** — 评估 + 平台管理

---

## 当前实现状态

前端项目已于 2026-06-02 完成初始实现，2026-06-04 完成 API 适配（Point 2）。开发服务器运行在 `http://localhost:5173/`（API 代理 → `http://localhost:8081`）。

### 已完成

| 类别 | 内容 | 状态 |
|------|------|------|
| 项目脚手架 | package.json, tsconfig.json, vite.config.ts, .gitignore | 完成 |
| 布局壳 | ConsoleLayout（48px 顶栏 + 8 模块导航 + 用户区）, ModuleLayout（200px 侧边栏 + Outlet） | 完成 |
| 路由框架 | router.tsx，24 条路由全部 `React.lazy()` + `Suspense` + `Spin` fallback | 完成 |
| 设计系统 | tokens.ts（Ant Design 5 ThemeConfig，主色 #597ef7，完整圆角/字号/组件 token 覆盖） | 完成 |
| API 客户端 | client.ts（Axios 实例 + 请求拦截器注入 X-User-Id/X-Dept-Id + 响应拦截器解包 ApiResponse + get/post/del 命名导出）, types.ts | 完成 |
| **8 个 API 模块** | agent.ts, skill.ts, model.ts, mcp.ts, eval.ts, platform.ts, workflow.ts, knowledge.ts — 全部创建完成 (2026-06-04) | 完成 |
| 共享组件 | StatCard, StatusTag, EmptyState, PageHeader | 完成 |
| Chat 模块 | ChatPage（会话列表 + 消息区 + 输入栏）, ChatInput, chatStore（Zustand，模拟 AI 延迟回复） | 完成 |
| Knowledge 模块 | KbListPage + KbCreateModal + knowledgeStore（真实后端 API 调用）, KbDetailPage + DocumentUploadPage | 完成 |
| Agent 模块 | AgentListPage + agentStore（真实 API）, AgentDetailPage, SessionHistoryPage | 完成 |
| Workflow 模块 | WorkflowListPage + workflowStore（真实 API）, WorkflowEditorPage（DAG 解析展示）, WorkflowInstancePage | 完成 |
| Skill 模块 | SkillListPage + skillStore（真实 API）, SkillDetailPage, SkillVersionHistory | 完成 |
| MCP 模块 | McpServerListPage + mcpStore（真实 API）, McpServerDetailPage（tools 分离获取）, McpToolListPage | 完成 |
| Model 模块 | ModelListPage + modelStore（真实 API）, ModelRouteRulePage, ModelCallLogPage | 完成 |
| Eval 模块 | EvalDatasetListPage + evalStore（真实 API）, EvalTaskListPage, EvalResultPage（results 数组展示） | 完成 |
| Platform 模块 | PromptVersionPage + platformStore（真实 API）, ApprovalPage | 完成 |
| 部署配置 | frontend Dockerfile（多阶段构建 node→nginx）, nginx.conf（SPA fallback + API 反向代理） | 完成 |
| **API 适配 (Point 2)** | 全部 8 个 store 从 mock 数据迁移至真实 API 调用，17 个页面字段对齐后端 API 实体类型，TypeScript 零错误 (2026-06-04) | 完成 |

### 实际目录结构与设计差异

设计规格中的部分组件在实际实现中做了简化合并：

| 设计规格 | 实际实现 | 原因 |
|---------|---------|------|
| ChatArea.tsx + MessageBubble.tsx | 合并入 ChatPage.tsx | 对话区逻辑较简单，独立拆分增加复杂度 |
| SearchBar.tsx | 未创建 | 各模块列表页暂不需要独立搜索组件 |
| useRequest.ts / usePagination.ts | 未创建 | async/await + useState 已满足当前需求 |
| DAGCanvas.tsx / NodeConfigPanel.tsx | WorkflowEditorPage（dag.nodes/edges 文本展示） | React Flow 集成延后 |
| ConnectorConfigCard / SearchEngineConfigCard | 合并入 KbDetailPage | 延后实现 |
| AgentFormModal / SkillFormModal / McpServerFormModal / ModelFormModal | 未创建 | 列表页展示为主，表单延后 |
| AnnotationPanel / PromptDiffView | 合并入对应页面 | 延后实现 |
| workflow.ts / agent.ts / skill.ts / mcp.ts / model.ts / eval.ts / platform.ts | **全部已创建 (2026-06-04)** | 8 个 API 模块全部实现，类型安全 |

### 待完善

1. **Workflow 模块**：集成 React Flow DAG 画布编辑器
2. **Knowledge 详情页**：连接器/搜索引擎配置界面
3. **各模块 CRUD 表单**：Agent/Skill/MCP/Model 的新建/编辑 Modal
4. **E2E 测试**：Playwright 关键用户流程测试
5. ~~**其余 API 文件**~~：已完成 (2026-06-04)
6. **SearchBar + useRequest/usePagination**：列表页数据量增长后提取通用组件
