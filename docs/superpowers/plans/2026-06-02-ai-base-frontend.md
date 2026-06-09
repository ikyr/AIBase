# AIBase 前端管理控制台实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建设 AIBase 统一管理控制台前端，首页即 Agent 对话，管理功能通过顶部 8 项导航按需访问。

**Architecture:** 模块化 SPA — 单个 React 应用，9 个懒加载路由组（chat + 8 业务模块），共享 ConsoleLayout 壳、API 客户端、设计系统。Zustand 按模块独立 store，无全局 store。

**Tech Stack:** React 18 + TypeScript 5 + Vite 5 + Ant Design 5 + React Router 6 + Zustand 4 + Axios 1

---

## 实现进度 (as of 2026-06-04)

### 已完成

| 任务 | 状态 | 备注 |
|------|------|------|
| Task 1: 项目脚手架 | Done | Vite + React + TS 项目搭建完成 |
| Task 2: 设计系统 Theme Tokens | Done | Ant Design 5 theme tokens 配置完成 |
| Task 3: API 客户端 + 类型定义 | Done | 扩展为 8 个 API 模块 (agent/skill/model/mcp/eval/platform/workflow/knowledge) |
| Task 4: 布局壳 | Done | ConsoleLayout + ModuleLayout 完成 |
| Task 5: 路由框架 + 占位页面 | Done | 24 条路由全部懒加载，无 404 |
| Task 6: 共享组件 | Done | StatCard, StatusTag, EmptyState, PageHeader |
| Task 7: Chat 模块 | Done | 会话列表 + 消息气泡 + 输入发送 (mock) |
| Task 8-12: 业务模块列表页 | Done | 全部 8 个模块列表页实现 |
| **API 适配 (Point 2)** | **Done** | 全部 store 从 mock 数据迁移到真实 API 调用 |

### Point 2 适配详情 (2026-06-04)

**API 模块 (8 个文件):**
- `src/shared/api/client.ts` — 添加 `get`/`post`/`del` 命名导出，`ApiResponse` 接口
- `src/shared/api/types.ts` — 添加 `KbConfigInfo.createdAt`/`updatedAt` 字段
- `src/shared/api/agent.ts` — AgentDef, AgentSession, AgentMessage 类型
- `src/shared/api/skill.ts` — SkillDef, SkillVersion, SkillExecutionLog 类型
- `src/shared/api/model.ts` — ModelConfig, ModelRouteRule, ModelCallLog 类型
- `src/shared/api/mcp.ts` — McpServer, McpTool 类型
- `src/shared/api/eval.ts` — EvalDataset, EvalTask, EvalResult 类型
- `src/shared/api/platform.ts` — PromptVersion, ApprovalRecord 类型
- `src/shared/api/workflow.ts` — WfDefinition, WfInstance, DagView 类型

**Store 适配 (8 个文件):**
- 全部 store 移除本地 mock 数据和接口定义
- 统一使用 `src/shared/api/*.ts` 中的类型和 API 函数
- `evalStore` 中 `result`/`fetchResult` 重命名为 `results`/`fetchResults` (数组)
- `workflowStore` 添加 `dag` 属性，`fetchDetail()` 并行获取 definition + DAG

**页面适配 (17 个文件):**
- 移除所有本地 `*Row` 接口，直接使用 API 实体类型
- 字段映射对齐后端实体 (如 `timestamp→createdAt`, `latency→durationMs`, `caller→callerRef`)
- `AgentDetailPage` 中 `skillIds`/`kbIds` 从逗号分隔字符串解析
- `KbDetailPage` 移除 documents/fetchDocuments，添加静态文档管理区域
- `EvalResultPage` 从单 result 改为 results 数组展示

---

### Task 1: 项目脚手架

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/package.json`
- Create: `D:/work/datang/AIBase/ai-base-frontend/index.html`
- Create: `D:/work/datang/AIBase/ai-base-frontend/vite.config.ts`
- Create: `D:/work/datang/AIBase/ai-base-frontend/tsconfig.json`
- Create: `D:/work/datang/AIBase/ai-base-frontend/tsconfig.node.json`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/main.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/App.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/vite-env.d.ts`

- [ ] **Step 1: Create project directory and package.json**

```bash
mkdir -p D:/work/datang/AIBase/ai-base-frontend/src
mkdir -p D:/work/datang/AIBase/ai-base-frontend/public
```

```json
{
  "name": "ai-base-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.26.0",
    "antd": "^5.20.0",
    "@ant-design/icons": "^5.4.0",
    "zustand": "^4.5.4",
    "axios": "^1.7.3",
    "dayjs": "^1.11.12"
  },
  "devDependencies": {
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "typescript": "^5.5.4",
    "vite": "^5.4.0"
  }
}
```

- [ ] **Step 2: Create index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AIBase</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 3: Create vite.config.ts**

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

- [ ] **Step 4: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "paths": {
      "@/*": ["./src/*"]
    },
    "baseUrl": "."
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 5: Create tsconfig.node.json**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 6: Create src/vite-env.d.ts**

```typescript
/// <reference types="vite/client" />
```

- [ ] **Step 7: Create minimal src/main.tsx and src/App.tsx**

```typescript
// src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

```typescript
// src/App.tsx
import { ConfigProvider } from 'antd';
import { themeTokens } from './shared/theme/tokens';

export default function App() {
  return (
    <ConfigProvider theme={themeTokens}>
      <div style={{ padding: 40, textAlign: 'center' }}>
        <h2>AIBase Console</h2>
      </div>
    </ConfigProvider>
  );
}
```

- [ ] **Step 8: Install dependencies and verify dev server starts**

```bash
cd D:/work/datang/AIBase/ai-base-frontend && npm install
```

Run: `npm run dev`
Expected: Vite dev server starts on http://localhost:5173, page renders "AIBase Console"

- [ ] **Step 9: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend && git commit -m "feat: scaffold frontend project with Vite + React + TypeScript + Ant Design"
```

---

### Task 2: 设计系统 Theme Tokens

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/theme/tokens.ts`

- [ ] **Step 1: Write Ant Design 5 theme token overrides**

```typescript
// src/shared/theme/tokens.ts
import type { ThemeConfig } from 'antd';

export const themeTokens: ThemeConfig = {
  token: {
    colorPrimary: '#597ef7',
    colorSuccess: '#16a34a',
    colorWarning: '#d97706',
    colorError: '#eb5757',
    colorInfo: '#597ef7',
    borderRadius: 8,
    borderRadiusLG: 12,
    borderRadiusSM: 6,
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    fontSize: 13,
    fontSizeHeading1: 20,
    fontSizeHeading2: 18,
    fontSizeHeading3: 16,
    fontSizeHeading4: 14,
    fontSizeLG: 14,
    fontSizeSM: 12,
    lineHeight: 1.5,
    controlHeight: 36,
    paddingContentHorizontal: 20,
    paddingContentVertical: 16,
    colorBgContainer: '#ffffff',
    colorBgLayout: '#f5f5f5',
  },
  components: {
    Button: {
      borderRadius: 8,
      borderRadiusLG: 10,
      borderRadiusSM: 6,
      controlHeight: 36,
      controlHeightLG: 42,
      controlHeightSM: 30,
      fontWeight: 500,
    },
    Card: {
      borderRadiusLG: 12,
      paddingLG: 18,
    },
    Input: {
      borderRadius: 10,
      borderRadiusLG: 12,
      borderRadiusSM: 8,
      controlHeight: 36,
      controlHeightLG: 42,
      controlHeightSM: 30,
    },
    Tag: {
      borderRadiusSM: 6,
    },
    Modal: {
      borderRadiusLG: 16,
    },
    Menu: {
      itemBorderRadius: 8,
      itemHeight: 36,
      itemMarginInline: 8,
    },
    Layout: {
      headerHeight: 48,
      siderWidth: 200,
    },
  },
};
```

- [ ] **Step 2: Verify theme applies**

Restart dev server, verify primary color renders as `#597ef7` (can add a test `<Button type="primary">Test</Button>` in App.tsx to verify).

- [ ] **Step 3: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/shared/theme && git commit -m "feat: add Ant Design theme token overrides"
```

---

### Task 3: API 客户端 + 类型定义

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/api/types.ts`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/api/client.ts`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/api/knowledge.ts`

- [ ] **Step 1: Write shared API types**

```typescript
// src/shared/api/types.ts

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: string | null;
}

export interface PageRequest {
  page: number;
  size: number;
}

export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

// --- Knowledge Base ---

export interface KbConfigInfo {
  id: string;
  name: string;
  description: string;
  kbType: 'PUBLIC' | 'PERSONAL' | 'DEPARTMENT';
  ownerId: string | null;
  ownerDeptId: string | null;
  embeddingModel: string;
  chunkSize: number;
  chunkOverlap: number;
  documentCount: number;
  status: string;
}

export interface KbCreateRequest {
  name: string;
  description?: string;
  kbType?: string;
  embeddingModel?: string;
  chunkSize?: number;
  chunkOverlap?: number;
}

export interface IngestRequest {
  kbId: string;
  title: string;
  content: string;
  sourceType?: 'UPLOAD' | 'CONNECTOR' | 'SEARCH' | 'API';
  sourceRef?: string;
  fileType?: string;
}

export interface IngestResult {
  docId: string;
  status: string;
  chunkCount: number;
}

export interface SearchRequest {
  kbId: string;
  query: string;
  topK?: number;
  strategy?: 'VECTOR' | 'KEYWORD' | 'HYBRID';
}

export interface SearchResult {
  query: string;
  tookMs: number;
  hits: SearchHit[];
}

export interface SearchHit {
  docId: string;
  chunkIndex: number;
  content: string;
  score: number;
  metadata?: Record<string, string>;
}

export interface KnowledgeStats {
  kbId: string;
  documentCount?: number;
  chunkCount?: number;
  totalTokens?: number;
}

// --- Agent ---

export interface AgentChatRequest {
  agentId?: string;
  sessionId?: string;
  message: string;
}

export interface AgentChatResponse {
  sessionId: string;
  message: string;
  toolCalls?: unknown[];
  done: boolean;
}

export interface SessionDetail {
  id: string;
  agentId: string;
  title: string;
  status: string;
  messages: unknown[];
}

// --- Workflow ---

export interface WorkflowExecuteRequest {
  definitionId: string;
  input: Record<string, unknown>;
}

export interface WorkflowResult {
  instanceId: string;
  status: string;
  output: Record<string, unknown> | null;
  errorMsg: string | null;
}
```

- [ ] **Step 2: Write Axios client with interceptors**

```typescript
// src/shared/api/client.ts
import axios from 'axios';
import type { ApiResponse } from './types';

const client = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — inject user context headers
client.interceptors.request.use((config) => {
  const userId = localStorage.getItem('userId') || 'dev-user';
  const deptId = localStorage.getItem('deptId') || 'dev-dept';
  config.headers['X-User-Id'] = userId;
  config.headers['X-Dept-Id'] = deptId;
  return config;
});

// Response interceptor — unwrap ApiResponse envelope
client.interceptors.response.use(
  (response) => response.data as ApiResponse<unknown>,
  (error) => {
    const message = error.response?.data?.error || error.message || 'Network error';
    console.error(`[API] ${error.config?.url}: ${message}`);
    return Promise.reject(error);
  }
);

export default client;
```

- [ ] **Step 3: Write knowledge API module (only module with real backend)**

```typescript
// src/shared/api/knowledge.ts
import client from './client';
import type { ApiResponse } from './types';
import type {
  KbConfigInfo,
  KbCreateRequest,
  IngestRequest,
  IngestResult,
  SearchRequest,
  SearchResult,
  KnowledgeStats,
} from './types';

export async function createKb(data: KbCreateRequest): Promise<ApiResponse<KbConfigInfo>> {
  return client.post('/knowledge/kb', data) as Promise<ApiResponse<KbConfigInfo>>;
}

export async function listKb(): Promise<ApiResponse<KbConfigInfo[]>> {
  return client.get('/knowledge/kb') as Promise<ApiResponse<KbConfigInfo[]>>;
}

export async function searchKb(data: SearchRequest): Promise<ApiResponse<SearchResult>> {
  return client.post('/knowledge/search', data) as Promise<ApiResponse<SearchResult>>;
}

export async function ingestDocument(data: IngestRequest): Promise<ApiResponse<IngestResult>> {
  return client.post('/knowledge/ingest', data) as Promise<ApiResponse<IngestResult>>;
}

export async function deleteDocument(docId: string): Promise<ApiResponse<null>> {
  return client.delete(`/knowledge/${docId}`) as Promise<ApiResponse<null>>;
}

export async function getKbStats(kbId: string): Promise<ApiResponse<KnowledgeStats>> {
  return client.get(`/knowledge/kb/${kbId}/stats`) as Promise<ApiResponse<KnowledgeStats>>;
}
```

- [ ] **Step 4: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/shared/api && git commit -m "feat: add API client, types, and knowledge API module"
```

---

### Task 4: 布局壳 — ConsoleLayout + ModuleLayout

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/layouts/ConsoleLayout.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/layouts/ConsoleLayout.css`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/layouts/ModuleLayout.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/stores/userStore.ts`

- [ ] **Step 1: Write userStore (shared auth state)**

```typescript
// src/shared/stores/userStore.ts
import { create } from 'zustand';

interface UserState {
  userId: string;
  deptId: string;
  userName: string;
  setUser: (userId: string, deptId: string, userName: string) => void;
}

export const useUserStore = create<UserState>((set) => ({
  userId: localStorage.getItem('userId') || 'dev-user',
  deptId: localStorage.getItem('deptId') || 'dev-dept',
  userName: localStorage.getItem('userName') || '开发者',
  setUser: (userId: string, deptId: string, userName: string) => {
    localStorage.setItem('userId', userId);
    localStorage.setItem('deptId', deptId);
    localStorage.setItem('userName', userName);
    set({ userId, deptId, userName });
  },
}));
```

- [ ] **Step 2: Write ConsoleLayout with top navigation bar**

```typescript
// src/layouts/ConsoleLayout.tsx
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { UserOutlined } from '@ant-design/icons';
import { useUserStore } from '../shared/stores/userStore';
import './ConsoleLayout.css';

interface NavItem {
  key: string;
  label: string;
  path: string;
  icon: string;
  group: 'business' | 'admin';
}

const navItems: NavItem[] = [
  { key: 'chat', label: '对话', path: '/', icon: '💬', group: 'business' },
  { key: 'workflow', label: '工作流', path: '/workflow', icon: '⚙️', group: 'business' },
  { key: 'knowledge', label: '知识库', path: '/knowledge', icon: '📚', group: 'business' },
  { key: 'agent', label: 'Agent', path: '/agent', icon: '🤖', group: 'admin' },
  { key: 'skill', label: 'Skill', path: '/skill', icon: '🛠', group: 'admin' },
  { key: 'mcp', label: 'MCP', path: '/mcp', icon: '🔌', group: 'admin' },
  { key: 'model', label: '模型', path: '/model', icon: '🧠', group: 'admin' },
  { key: 'eval', label: '评估', path: '/eval', icon: '📊', group: 'admin' },
];

export default function ConsoleLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const userName = useUserStore((s) => s.userName);

  const isActive = (item: NavItem) => {
    if (item.path === '/') return location.pathname === '/';
    return location.pathname.startsWith(item.path);
  };

  const businessItems = navItems.filter((i) => i.group === 'business');
  const adminItems = navItems.filter((i) => i.group === 'admin');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      {/* Top navigation bar */}
      <header className="console-topbar">
        <div className="console-topbar-left">
          <span
            className="console-logo"
            onClick={() => navigate('/')}
            style={{ cursor: 'pointer' }}
          >
            AI<span className="console-logo-highlight">Base</span>
          </span>
          <nav className="console-nav">
            {businessItems.map((item) => (
              <button
                key={item.key}
                className={`console-nav-item${isActive(item) ? ' active' : ''}`}
                onClick={() => navigate(item.path)}
              >
                <span className="console-nav-icon">{item.icon}</span>
                {item.label}
              </button>
            ))}
            <span className="console-nav-divider" />
            {adminItems.map((item) => (
              <button
                key={item.key}
                className={`console-nav-item${isActive(item) ? ' active' : ''}`}
                onClick={() => navigate(item.path)}
              >
                <span className="console-nav-icon">{item.icon}</span>
                {item.label}
              </button>
            ))}
          </nav>
        </div>
        <div className="console-user-chip">
          <UserOutlined style={{ fontSize: 12 }} />
          <span>{userName}</span>
        </div>
      </header>

      {/* Page content */}
      <main style={{ flex: 1, overflow: 'hidden' }}>
        <Outlet />
      </main>
    </div>
  );
}
```

- [ ] **Step 3: Write ConsoleLayout.css**

```css
/* src/layouts/ConsoleLayout.css */

.console-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.console-topbar-left {
  display: flex;
  align-items: center;
  gap: 0;
}

.console-logo {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
  margin-right: 24px;
  user-select: none;
}

.console-logo-highlight {
  color: #597ef7;
}

.console-nav {
  display: flex;
  align-items: center;
  gap: 2px;
}

.console-nav-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  color: #666;
  border-radius: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
  white-space: nowrap;
}

.console-nav-item:hover {
  background: #f5f5f5;
  color: #333;
}

.console-nav-item.active {
  background: #f0f5ff;
  color: #597ef7;
  font-weight: 500;
}

.console-nav-icon {
  font-size: 14px;
  line-height: 1;
}

.console-nav-divider {
  width: 1px;
  height: 18px;
  background: #ebebeb;
  margin: 0 8px;
}

.console-user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 8px;
  background: #f5f5f5;
  font-size: 12px;
  color: #555;
  cursor: default;
}
```

- [ ] **Step 4: Write ModuleLayout (sidebar + content for admin pages)**

```typescript
// src/layouts/ModuleLayout.tsx
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

interface SideMenuItem {
  key: string;
  label: string;
  path: string;
}

interface ModuleLayoutProps {
  title: string;
  menu: SideMenuItem[];
  children?: React.ReactNode;
}

export default function ModuleLayout({ title, menu, children }: ModuleLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      {/* Sidebar */}
      <aside
        style={{
          width: 200,
          background: '#fafafa',
          borderRight: '1px solid #f0f0f0',
          padding: '12px 8px',
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
          flexShrink: 0,
        }}
      >
        <div
          style={{
            fontSize: 11,
            color: '#999',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            fontWeight: 600,
            padding: '8px 10px 4px',
          }}
        >
          {title}
        </div>
        {menu.map((item) => (
          <button
            key={item.key}
            onClick={() => navigate(item.path)}
            style={{
              display: 'block',
              width: '100%',
              textAlign: 'left',
              padding: '7px 10px',
              fontSize: 13,
              color: location.pathname === item.path ? '#597ef7' : '#555',
              fontWeight: location.pathname === item.path ? 500 : 400,
              borderRadius: 8,
              border: 'none',
              background:
                location.pathname === item.path ? '#fff' : 'transparent',
              boxShadow:
                location.pathname === item.path
                  ? '0 1px 3px rgba(0,0,0,0.04)'
                  : 'none',
              cursor: 'pointer',
              transition: 'all 0.15s',
              fontFamily: 'inherit',
            }}
          >
            {item.label}
          </button>
        ))}
      </aside>

      {/* Content */}
      <div style={{ flex: 1, overflow: 'auto', background: '#fff' }}>
        {children ?? <Outlet />}
      </div>
    </div>
  );
}
```

- [ ] **Step 5: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/layouts ai-base-frontend/src/shared/stores && git commit -m "feat: add ConsoleLayout and ModuleLayout shells with top nav and sidebar"
```

---

### Task 5: 路由框架 + 占位页面

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/router.tsx`
- Create placeholder pages for all 9 modules (lazy-loaded)
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/App.tsx`
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/main.tsx`

- [ ] **Step 1: Create placeholder page factory and all placeholder pages**

```typescript
// src/modules/chat/pages/ChatPage.tsx
export default function ChatPage() {
  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <div style={{ width: 200, background: '#fafafa', borderRight: '1px solid #f0f0f0', padding: 12 }}>
        <div style={{ fontSize: 11, color: '#999', fontWeight: 600, padding: '8px 10px 4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>会话历史</div>
      </div>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#999', gap: 8 }}>
        <span style={{ fontSize: 32 }}>💬</span>
        <span style={{ fontSize: 14, fontWeight: 500 }}>开始一段新对话</span>
        <span style={{ fontSize: 12 }}>选择一个 Agent 或直接输入问题</span>
      </div>
    </div>
  );
}
```

```typescript
// src/modules/workflow/pages/WorkflowListPage.tsx
export default function WorkflowListPage() {
  return <PlaceholderPage icon="⚙️" title="工作流列表" />;
}
function PlaceholderPage({ icon, title }: { icon: string; title: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#999', gap: 8, flexDirection: 'column' }}>
      <span style={{ fontSize: 32 }}>{icon}</span>
      <span style={{ fontSize: 14, fontWeight: 500 }}>{title}</span>
      <span style={{ fontSize: 12 }}>即将上线</span>
    </div>
  );
}
```

- [ ] **Step 2: Create all remaining placeholder pages**

For each of the following files, use the same `PlaceholderPage` pattern with the appropriate icon and title:

```
src/modules/workflow/pages/WorkflowListPage.tsx         → ⚙️ 工作流列表
src/modules/workflow/pages/WorkflowEditorPage.tsx        → ⚙️ 工作流编辑器
src/modules/workflow/pages/WorkflowInstancePage.tsx      → ⚙️ 工作流实例
src/modules/knowledge/pages/KbListPage.tsx               → 📚 知识库列表
src/modules/knowledge/pages/KbDetailPage.tsx             → 📚 知识库详情
src/modules/knowledge/pages/DocumentUploadPage.tsx       → 📚 文档上传
src/modules/agent/pages/AgentListPage.tsx                → 🤖 Agent 列表
src/modules/agent/pages/AgentDetailPage.tsx              → 🤖 Agent 详情
src/modules/agent/pages/SessionHistoryPage.tsx           → 🤖 会话记录
src/modules/skill/pages/SkillListPage.tsx                → 🛠 Skill 列表
src/modules/skill/pages/SkillDetailPage.tsx              → 🛠 Skill 详情
src/modules/skill/pages/SkillVersionHistory.tsx          → 🛠 版本历史
src/modules/mcp/pages/McpServerListPage.tsx              → 🔌 MCP Server 列表
src/modules/mcp/pages/McpServerDetailPage.tsx            → 🔌 MCP Server 详情
src/modules/mcp/pages/McpToolListPage.tsx                → 🔌 工具列表
src/modules/model/pages/ModelListPage.tsx                → 🧠 模型列表
src/modules/model/pages/ModelRouteRulePage.tsx           → 🧠 路由规则
src/modules/model/pages/ModelCallLogPage.tsx             → 🧠 调用日志
src/modules/eval/pages/EvalDatasetListPage.tsx           → 📊 数据集列表
src/modules/eval/pages/EvalTaskListPage.tsx              → 📊 评估任务
src/modules/eval/pages/EvalResultPage.tsx                → 📊 评估结果
src/modules/platform/pages/PromptVersionPage.tsx         → 📋 Prompt 版本
src/modules/platform/pages/ApprovalPage.tsx              → 📋 审批工作台
```

Each file follows this pattern (export a default component, use an inline `PlaceholderPage` or import from shared):

```typescript
// Example: src/modules/skill/pages/SkillListPage.tsx
export default function SkillListPage() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#999', gap: 8, flexDirection: 'column' }}>
      <span style={{ fontSize: 32 }}>🛠</span>
      <span style={{ fontSize: 14, fontWeight: 500 }}>Skill 列表</span>
      <span style={{ fontSize: 12 }}>即将上线</span>
    </div>
  );
}
```

- [ ] **Step 3: Write router.tsx with lazy loading**

```typescript
// src/router.tsx
import { lazy, Suspense } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { Spin } from 'antd';
import ConsoleLayout from './layouts/ConsoleLayout';
import ModuleLayout from './layouts/ModuleLayout';

// Lazy load all pages
const ChatPage = lazy(() => import('./modules/chat/pages/ChatPage'));

const WorkflowListPage = lazy(() => import('./modules/workflow/pages/WorkflowListPage'));
const WorkflowEditorPage = lazy(() => import('./modules/workflow/pages/WorkflowEditorPage'));
const WorkflowInstancePage = lazy(() => import('./modules/workflow/pages/WorkflowInstancePage'));

const KbListPage = lazy(() => import('./modules/knowledge/pages/KbListPage'));
const KbDetailPage = lazy(() => import('./modules/knowledge/pages/KbDetailPage'));
const DocumentUploadPage = lazy(() => import('./modules/knowledge/pages/DocumentUploadPage'));

const AgentListPage = lazy(() => import('./modules/agent/pages/AgentListPage'));
const AgentDetailPage = lazy(() => import('./modules/agent/pages/AgentDetailPage'));
const SessionHistoryPage = lazy(() => import('./modules/agent/pages/SessionHistoryPage'));

const SkillListPage = lazy(() => import('./modules/skill/pages/SkillListPage'));
const SkillDetailPage = lazy(() => import('./modules/skill/pages/SkillDetailPage'));
const SkillVersionHistory = lazy(() => import('./modules/skill/pages/SkillVersionHistory'));

const McpServerListPage = lazy(() => import('./modules/mcp/pages/McpServerListPage'));
const McpServerDetailPage = lazy(() => import('./modules/mcp/pages/McpServerDetailPage'));
const McpToolListPage = lazy(() => import('./modules/mcp/pages/McpToolListPage'));

const ModelListPage = lazy(() => import('./modules/model/pages/ModelListPage'));
const ModelRouteRulePage = lazy(() => import('./modules/model/pages/ModelRouteRulePage'));
const ModelCallLogPage = lazy(() => import('./modules/model/pages/ModelCallLogPage'));

const EvalDatasetListPage = lazy(() => import('./modules/eval/pages/EvalDatasetListPage'));
const EvalTaskListPage = lazy(() => import('./modules/eval/pages/EvalTaskListPage'));
const EvalResultPage = lazy(() => import('./modules/eval/pages/EvalResultPage'));

const PromptVersionPage = lazy(() => import('./modules/platform/pages/PromptVersionPage'));
const ApprovalPage = lazy(() => import('./modules/platform/pages/ApprovalPage'));

function Lazy({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<Spin style={{ display: 'block', margin: '40px auto' }} />}>{children}</Suspense>;
}

// Sidebar menu configs for each module
const workflowMenu = [
  { key: 'list', label: '工作流列表', path: '/workflow' },
];
const knowledgeMenu = [
  { key: 'list', label: '知识库列表', path: '/knowledge' },
];
const agentMenu = [
  { key: 'list', label: 'Agent 列表', path: '/agent' },
  { key: 'sessions', label: '会话记录', path: '/agent/sessions' },
];
const skillMenu = [
  { key: 'list', label: 'Skill 列表', path: '/skill' },
];
const mcpMenu = [
  { key: 'servers', label: 'Server 列表', path: '/mcp' },
  { key: 'tools', label: '工具列表', path: '/mcp/tools' },
];
const modelMenu = [
  { key: 'models', label: '模型列表', path: '/model' },
  { key: 'routes', label: '路由规则', path: '/model/routes' },
  { key: 'logs', label: '调用日志', path: '/model/logs' },
];
const evalMenu = [
  { key: 'datasets', label: '数据集', path: '/eval' },
  { key: 'tasks', label: '评估任务', path: '/eval/tasks' },
];
const platformMenu = [
  { key: 'prompts', label: 'Prompt 版本', path: '/platform/prompts' },
  { key: 'approvals', label: '审批工作台', path: '/platform/approvals' },
];

export const router = createBrowserRouter([
  {
    element: <ConsoleLayout />,
    children: [
      { index: true, element: <Lazy><ChatPage /></Lazy> },
      {
        path: 'workflow',
        element: <ModuleLayout title="工作流管理" menu={workflowMenu} />,
        children: [
          { index: true, element: <Lazy><WorkflowListPage /></Lazy> },
          { path: ':id', element: <Lazy><WorkflowEditorPage /></Lazy> },
          { path: 'instance/:id', element: <Lazy><WorkflowInstancePage /></Lazy> },
        ],
      },
      {
        path: 'knowledge',
        element: <ModuleLayout title="知识库管理" menu={knowledgeMenu} />,
        children: [
          { index: true, element: <Lazy><KbListPage /></Lazy> },
          { path: ':id', element: <Lazy><KbDetailPage /></Lazy> },
          { path: ':id/upload', element: <Lazy><DocumentUploadPage /></Lazy> },
        ],
      },
      {
        path: 'agent',
        element: <ModuleLayout title="Agent 管理" menu={agentMenu} />,
        children: [
          { index: true, element: <Lazy><AgentListPage /></Lazy> },
          { path: ':id', element: <Lazy><AgentDetailPage /></Lazy> },
          { path: 'sessions', element: <Lazy><SessionHistoryPage /></Lazy> },
        ],
      },
      {
        path: 'skill',
        element: <ModuleLayout title="Skill 管理" menu={skillMenu} />,
        children: [
          { index: true, element: <Lazy><SkillListPage /></Lazy> },
          { path: ':id', element: <Lazy><SkillDetailPage /></Lazy> },
          { path: ':id/versions', element: <Lazy><SkillVersionHistory /></Lazy> },
        ],
      },
      {
        path: 'mcp',
        element: <ModuleLayout title="MCP 管理" menu={mcpMenu} />,
        children: [
          { index: true, element: <Lazy><McpServerListPage /></Lazy> },
          { path: ':id', element: <Lazy><McpServerDetailPage /></Lazy> },
          { path: 'tools', element: <Lazy><McpToolListPage /></Lazy> },
        ],
      },
      {
        path: 'model',
        element: <ModuleLayout title="模型管理" menu={modelMenu} />,
        children: [
          { index: true, element: <Lazy><ModelListPage /></Lazy> },
          { path: 'routes', element: <Lazy><ModelRouteRulePage /></Lazy> },
          { path: 'logs', element: <Lazy><ModelCallLogPage /></Lazy> },
        ],
      },
      {
        path: 'eval',
        element: <ModuleLayout title="评估管理" menu={evalMenu} />,
        children: [
          { index: true, element: <Lazy><EvalDatasetListPage /></Lazy> },
          { path: 'tasks', element: <Lazy><EvalTaskListPage /></Lazy> },
          { path: 'results/:id', element: <Lazy><EvalResultPage /></Lazy> },
        ],
      },
      {
        path: 'platform',
        element: <ModuleLayout title="平台管理" menu={platformMenu} />,
        children: [
          { path: 'prompts', element: <Lazy><PromptVersionPage /></Lazy> },
          { path: 'approvals', element: <Lazy><ApprovalPage /></Lazy> },
        ],
      },
    ],
  },
]);
```

- [ ] **Step 4: Update App.tsx to use RouterProvider**

```typescript
// src/App.tsx
import { RouterProvider } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import { themeTokens } from './shared/theme/tokens';
import { router } from './router';

export default function App() {
  return (
    <ConfigProvider theme={themeTokens}>
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}
```

- [ ] **Step 5: Verify — run dev server and test navigation**

Run: `cd D:/work/datang/AIBase/ai-base-frontend && npm run dev`

Verify:
- Homepage `/` shows chat placeholder
- Click "工作流" → `/workflow` shows placeholder with sidebar
- Click "Agent" → `/agent` shows placeholder with sidebar
- All 8 nav items navigate correctly, URL changes, sidebar renders

- [ ] **Step 6: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/router.tsx ai-base-frontend/src/App.tsx ai-base-frontend/src/modules && git commit -m "feat: add router with lazy-loaded placeholder pages for all 9 modules"
```

---

### Task 6: 共享组件

**Files:**
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/components/StatCard.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/components/StatusTag.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/components/EmptyState.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/shared/components/PageHeader.tsx`

- [ ] **Step 1: Write StatCard**

```typescript
// src/shared/components/StatCard.tsx
interface StatCardProps {
  value: number | string;
  label: string;
}

export default function StatCard({ value, label }: StatCardProps) {
  return (
    <div
      style={{
        background: '#fafafa',
        borderRadius: 12,
        padding: '14px 16px',
        minWidth: 120,
        flex: 1,
      }}
    >
      <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.5px', lineHeight: 1.2 }}>
        {value}
      </div>
      <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{label}</div>
    </div>
  );
}
```

- [ ] **Step 2: Write StatusTag**

```typescript
// src/shared/components/StatusTag.tsx
const colorMap: Record<string, { bg: string; color: string }> = {
  ACTIVE: { bg: '#f0f5ff', color: '#597ef7' },
  SUCCESS: { bg: '#f0fdf4', color: '#16a34a' },
  COMPLETED: { bg: '#f0fdf4', color: '#16a34a' },
  PENDING: { bg: '#fffcf0', color: '#d97706' },
  RUNNING: { bg: '#f0f5ff', color: '#597ef7' },
  FAILED: { bg: '#fff0f0', color: '#eb5757' },
  ERROR: { bg: '#fff0f0', color: '#eb5757' },
  DRAFT: { bg: '#f5f5f5', color: '#666' },
  DISCONNECTED: { bg: '#f5f5f5', color: '#666' },
  UNKNOWN: { bg: '#f5f5f5', color: '#999' },
};

interface StatusTagProps {
  status: string;
}

export default function StatusTag({ status }: StatusTagProps) {
  const c = colorMap[status] ?? { bg: '#f5f5f5', color: '#666' };
  return (
    <span
      style={{
        display: 'inline-block',
        padding: '3px 10px',
        borderRadius: 6,
        fontSize: 11,
        fontWeight: 500,
        background: c.bg,
        color: c.color,
        lineHeight: 1.5,
      }}
    >
      {status}
    </span>
  );
}
```

- [ ] **Step 3: Write EmptyState**

```typescript
// src/shared/components/EmptyState.tsx
import { Button } from 'antd';

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export default function EmptyState({ icon = '📭', title, description, actionLabel, onAction }: EmptyStateProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 60, color: '#999', gap: 8 }}>
      <span style={{ fontSize: 40, lineHeight: 1 }}>{icon}</span>
      <span style={{ fontSize: 14, fontWeight: 500, color: '#555' }}>{title}</span>
      {description && <span style={{ fontSize: 12 }}>{description}</span>}
      {actionLabel && onAction && (
        <Button type="primary" onClick={onAction} style={{ marginTop: 8 }}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
```

- [ ] **Step 4: Write PageHeader**

```typescript
// src/shared/components/PageHeader.tsx
import { Button } from 'antd';

interface PageHeaderProps {
  title: string;
  actionLabel?: string;
  onAction?: () => void;
  children?: React.ReactNode;
}

export default function PageHeader({ title, actionLabel, onAction, children }: PageHeaderProps) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 16,
        padding: '20px 20px 0',
      }}
    >
      <div>
        <h2 style={{ fontSize: 20, fontWeight: 700, margin: 0, letterSpacing: '-0.3px' }}>{title}</h2>
        {children}
      </div>
      {actionLabel && onAction && (
        <Button type="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
```

- [ ] **Step 5: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/shared/components && git commit -m "feat: add shared components — StatCard, StatusTag, EmptyState, PageHeader"
```

---

### Task 7: Chat 模块（首页对话）

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/chat/pages/ChatPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/chat/components/SessionList.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/chat/components/MessageBubble.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/chat/components/ChatInput.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/chat/stores/chatStore.ts`

- [ ] **Step 1: Write chatStore**

```typescript
// src/modules/chat/stores/chatStore.ts
import { create } from 'zustand';

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface Session {
  id: string;
  title: string;
  lastMessage: string;
  updatedAt: number;
}

interface ChatState {
  sessions: Session[];
  activeSessionId: string | null;
  messages: Message[];
  loading: boolean;
  createSession: () => void;
  switchSession: (id: string) => void;
  sendMessage: (content: string) => Promise<void>;
}

let nextId = 1;

export const useChatStore = create<ChatState>((set, get) => ({
  sessions: [],
  activeSessionId: null,
  messages: [],
  loading: false,

  createSession: () => {
    const id = `session-${nextId++}`;
    const session: Session = {
      id,
      title: '新对话',
      lastMessage: '',
      updatedAt: Date.now(),
    };
    set((s) => ({
      sessions: [session, ...s.sessions],
      activeSessionId: id,
      messages: [],
    }));
  },

  switchSession: (id: string) => {
    set({ activeSessionId: id, messages: [] });
  },

  sendMessage: async (content: string) => {
    const { activeSessionId, messages } = get();
    if (!activeSessionId) return;

    const userMsg: Message = {
      id: `msg-${nextId++}`,
      role: 'user',
      content,
      timestamp: Date.now(),
    };

    set({ messages: [...messages, userMsg], loading: true });

    // Simulate AI response (real API call when backend ready)
    await new Promise((r) => setTimeout(r, 800));

    const aiMsg: Message = {
      id: `msg-${nextId++}`,
      role: 'assistant',
      content: `收到你的消息：「${content}」。当前为模拟回复，后端 Agent 服务接入后将返回真实 AI 响应。`,
      timestamp: Date.now(),
    };

    set((s) => ({
      messages: [...s.messages, aiMsg],
      loading: false,
      sessions: s.sessions.map((sess) =>
        sess.id === activeSessionId
          ? { ...sess, lastMessage: content, updatedAt: Date.now(), title: content.slice(0, 20) }
          : sess
      ),
    }));
  },
}));
```

- [ ] **Step 2: Write SessionList**

```typescript
// src/modules/chat/components/SessionList.tsx
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useChatStore } from '../stores/chatStore';

export default function SessionList() {
  const sessions = useChatStore((s) => s.sessions);
  const activeId = useChatStore((s) => s.activeSessionId);
  const createSession = useChatStore((s) => s.createSession);
  const switchSession = useChatStore((s) => s.switchSession);

  return (
    <div style={{ width: 200, background: '#fafafa', borderRight: '1px solid #f0f0f0', display: 'flex', flexDirection: 'column', flexShrink: 0 }}>
      <div style={{ padding: '12px 8px', borderBottom: '1px solid #f0f0f0' }}>
        <Button block icon={<PlusOutlined />} onClick={createSession} size="small">
          新建会话
        </Button>
      </div>
      <div style={{ flex: 1, overflow: 'auto', padding: '8px', display: 'flex', flexDirection: 'column', gap: 2 }}>
        {sessions.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#999', fontSize: 12, padding: 20 }}>
            暂无会话，点击上方按钮开始
          </div>
        ) : (
          sessions.map((s) => (
            <button
              key={s.id}
              onClick={() => switchSession(s.id)}
              style={{
                width: '100%',
                textAlign: 'left',
                padding: '10px',
                borderRadius: 8,
                border: 'none',
                background: s.id === activeId ? '#fff' : 'transparent',
                boxShadow: s.id === activeId ? '0 1px 3px rgba(0,0,0,0.04)' : 'none',
                cursor: 'pointer',
                fontFamily: 'inherit',
                transition: 'all 0.15s',
              }}
            >
              <div style={{ fontSize: 13, fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {s.title}
              </div>
              <div style={{ fontSize: 11, color: '#999', marginTop: 2 }}>
                {new Date(s.updatedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
              </div>
            </button>
          ))
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Write MessageBubble**

```typescript
// src/modules/chat/components/MessageBubble.tsx
import type { Message } from '../stores/chatStore';

interface MessageBubbleProps {
  message: Message;
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'user';
  return (
    <div style={{ display: 'flex', justifyContent: isUser ? 'flex-end' : 'flex-start', marginBottom: 6 }}>
      <div
        style={{
          maxWidth: '80%',
          padding: '10px 14px',
          borderRadius: 12,
          borderBottomLeftRadius: isUser ? 12 : 4,
          borderBottomRightRadius: isUser ? 4 : 12,
          background: isUser ? '#f0f5ff' : '#f5f5f5',
          color: isUser ? '#1a1a1a' : '#333',
          fontSize: 13,
          lineHeight: 1.5,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
        }}
      >
        {message.content}
      </div>
    </div>
  );
}
```

- [ ] **Step 4: Write ChatInput**

```typescript
// src/modules/chat/components/ChatInput.tsx
import { useState, useRef, useEffect } from 'react';
import { Button } from 'antd';
import { SendOutlined } from '@ant-design/icons';
import { useChatStore } from '../stores/chatStore';

export default function ChatInput() {
  const [value, setValue] = useState('');
  const sendMessage = useChatStore((s) => s.sendMessage);
  const loading = useChatStore((s) => s.loading);
  const activeSessionId = useChatStore((s) => s.activeSessionId);
  const createSession = useChatStore((s) => s.createSession);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, [activeSessionId]);

  const handleSend = () => {
    const content = value.trim();
    if (!content || loading) return;
    if (!activeSessionId) createSession();
    // sendMessage reads activeSessionId from store on next tick
    setTimeout(() => {
      useChatStore.getState().sendMessage(content);
    }, 0);
    setValue('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        gap: 8,
        padding: '12px 16px',
        borderTop: '1px solid #f0f0f0',
        background: '#fafafa',
        alignItems: 'flex-end',
      }}
    >
      <textarea
        ref={inputRef}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
        rows={1}
        style={{
          flex: 1,
          resize: 'none',
          border: '1px solid #e0e0e0',
          borderRadius: 10,
          padding: '8px 12px',
          fontSize: 13,
          fontFamily: 'inherit',
          lineHeight: 1.5,
          outline: 'none',
          maxHeight: 120,
        }}
      />
      <Button
        type="primary"
        icon={<SendOutlined />}
        onClick={handleSend}
        loading={loading}
        style={{ borderRadius: 10 }}
      >
        发送
      </Button>
    </div>
  );
}
```

- [ ] **Step 5: Rewrite ChatPage with real components**

```typescript
// src/modules/chat/pages/ChatPage.tsx
import { useEffect, useRef } from 'react';
import SessionList from '../components/SessionList';
import MessageBubble from '../components/MessageBubble';
import ChatInput from '../components/ChatInput';
import { useChatStore } from '../stores/chatStore';
import EmptyState from '../../../shared/components/EmptyState';

export default function ChatPage() {
  const messages = useChatStore((s) => s.messages);
  const activeSessionId = useChatStore((s) => s.activeSessionId);
  const createSession = useChatStore((s) => s.createSession);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <SessionList />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#fff' }}>
        {!activeSessionId ? (
          <EmptyState
            icon="💬"
            title="开始一段新对话"
            description="选择或创建一个 Agent 会话，开始 AI 驱动的智能对话"
            actionLabel="新建会话"
            onAction={createSession}
          />
        ) : (
          <>
            <div style={{ flex: 1, overflow: 'auto', padding: '20px' }}>
              {messages.length === 0 ? (
                <div style={{ textAlign: 'center', color: '#999', marginTop: 40, fontSize: 13 }}>
                  发送一条消息开始对话
                </div>
              ) : (
                messages.map((msg) => <MessageBubble key={msg.id} message={msg} />)
              )}
              <div ref={bottomRef} />
            </div>
            <ChatInput />
          </>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 6: Verify chat flow**

Run `npm run dev`, verify:
- Homepage shows empty state with "新建会话" button
- Click button → creates session, shows input bar
- Type message, press Enter → user bubble appears, loading indicator, AI response appears
- Create multiple sessions → switch between them

- [ ] **Step 7: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules/chat ai-base-frontend/src/shared/components/EmptyState.tsx && git commit -m "feat: implement ChatPage with session list, message bubbles, and chat input"
```

---

### Task 8: Knowledge 模块 — 知识库列表页

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/knowledge/pages/KbListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/knowledge/stores/knowledgeStore.ts`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/knowledge/components/KbCreateModal.tsx`

- [ ] **Step 1: Write knowledgeStore**

```typescript
// src/modules/knowledge/stores/knowledgeStore.ts
import { create } from 'zustand';
import { listKb, createKb } from '../../../shared/api/knowledge';
import type { KbConfigInfo, KbCreateRequest } from '../../../shared/api/types';

interface KnowledgeState {
  kbs: KbConfigInfo[];
  loading: boolean;
  error: string | null;
  fetchList: () => Promise<void>;
  create: (req: KbCreateRequest) => Promise<void>;
}

export const useKnowledgeStore = create<KnowledgeState>((set) => ({
  kbs: [],
  loading: false,
  error: null,

  fetchList: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listKb();
      if (res.success && res.data) {
        set({ kbs: res.data, loading: false });
      } else {
        set({ error: res.error || 'Failed to load', loading: false });
      }
    } catch {
      set({ error: 'Network error', loading: false });
    }
  },

  create: async (req: KbCreateRequest) => {
    set({ loading: true, error: null });
    try {
      const res = await createKb(req);
      if (res.success) {
        // Refresh list
        const listRes = await listKb();
        if (listRes.success && listRes.data) {
          set({ kbs: listRes.data, loading: false });
        }
      } else {
        set({ error: res.error || 'Failed to create', loading: false });
      }
    } catch {
      set({ error: 'Network error', loading: false });
    }
  },
}));
```

- [ ] **Step 2: Write KbCreateModal**

```typescript
// src/modules/knowledge/components/KbCreateModal.tsx
import { Modal, Form, Input, Select, InputNumber } from 'antd';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import type { KbCreateRequest } from '../../../shared/api/types';

interface KbCreateModalProps {
  open: boolean;
  onClose: () => void;
}

export default function KbCreateModal({ open, onClose }: KbCreateModalProps) {
  const [form] = Form.useForm<KbCreateRequest>();
  const create = useKnowledgeStore((s) => s.create);
  const loading = useKnowledgeStore((s) => s.loading);

  const handleOk = async () => {
    const values = await form.validateFields();
    await create(values);
    onClose();
    form.resetFields();
  };

  return (
    <Modal
      title="创建知识库"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={loading}
      okText="创建"
      cancelText="取消"
    >
      <Form form={form} layout="vertical" initialValues={{ kbType: 'PUBLIC', embeddingModel: 'text-embedding-v3', chunkSize: 800, chunkOverlap: 100 }}>
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入知识库名称' }]}>
          <Input placeholder="我的知识库" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="知识库用途说明" />
        </Form.Item>
        <Form.Item name="kbType" label="类型">
          <Select options={[
            { value: 'PUBLIC', label: '公共' },
            { value: 'PERSONAL', label: '个人' },
            { value: 'DEPARTMENT', label: '部门' },
          ]} />
        </Form.Item>
        <Form.Item name="embeddingModel" label="Embedding 模型">
          <Select options={[{ value: 'text-embedding-v3', label: 'text-embedding-v3' }]} />
        </Form.Item>
        <Form.Item name="chunkSize" label="分块大小">
          <InputNumber min={100} max={4000} />
        </Form.Item>
        <Form.Item name="chunkOverlap" label="重叠大小">
          <InputNumber min={0} max={500} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
```

- [ ] **Step 3: Rewrite KbListPage**

```typescript
// src/modules/knowledge/pages/KbListPage.tsx
import { useEffect, useState } from 'react';
import { Spin, Alert, Button, Card, Popconfirm } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import KbCreateModal from '../components/KbCreateModal';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function KbListPage() {
  const { kbs, loading, error, fetchList } = useKnowledgeStore();
  const [modalOpen, setModalOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="知识库" actionLabel="新建知识库" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={kbs.length} label="知识库总数" />
          <StatCard value={kbs.filter((k) => k.status === 'ACTIVE').length} label="运行中" />
          <StatCard value={kbs.reduce((sum, k) => sum + (k.documentCount || 0), 0)} label="文档总数" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 12 }} closable />}
        {loading && kbs.length === 0 ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : kbs.length === 0 ? (
          <EmptyState icon="📚" title="暂无知识库" description="创建你的第一个知识库" actionLabel="新建知识库" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {kbs.map((kb) => (
              <Card
                key={kb.id}
                hoverable
                onClick={() => navigate(`/knowledge/${kb.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{kb.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {kb.description || '无描述'} · {kb.kbType} · Chunk {kb.chunkSize}/{kb.chunkOverlap} · Docs: {kb.documentCount}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <StatusTag status={kb.status} />
                    <Popconfirm title="确定删除此知识库？">
                      <Button size="small" danger icon={<DeleteOutlined />} onClick={(e) => e.stopPropagation()} />
                    </Popconfirm>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <KbCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
```

- [ ] **Step 4: Verify knowledge list**

Run `npm run dev`, navigate to `/knowledge`:
- Verify stats cards render
- Click "新建知识库" → modal opens
- Fill form → submit → calls backend → new KB appears in list
- Click card → navigates to `/knowledge/:id` (placeholder page)

- [ ] **Step 5: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules/knowledge && git commit -m "feat: implement knowledge base list page with create modal and stats"
```

---

### Task 9: Agent 模块 — Agent 列表页

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/agent/pages/AgentListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/agent/stores/agentStore.ts`

- [ ] **Step 1: Write agentStore**

```typescript
// src/modules/agent/stores/agentStore.ts
import { create } from 'zustand';

interface AgentDef {
  id: string;
  name: string;
  description: string;
  model: string;
  coordinationMode: string;
  skillCount: number;
  kbCount: number;
  status: string;
}

interface AgentState {
  agents: AgentDef[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

// Mock data until backend agent service is ready
const mockAgents: AgentDef[] = [
  { id: '1', name: '申报书写作Agent', description: '根据项目基本信息生成完整的申报书', model: 'qwen-max', coordinationMode: 'GRAPH', skillCount: 4, kbCount: 2, status: 'ACTIVE' },
  { id: '2', name: '立项评审Agent', description: '多维度评分+评审意见生成', model: 'qwen-plus', coordinationMode: 'REACT', skillCount: 2, kbCount: 1, status: 'ACTIVE' },
  { id: '3', name: '合规审查Agent', description: '政策法规匹配与异常检测', model: 'deepseek-v3', coordinationMode: 'REACT', skillCount: 3, kbCount: 1, status: 'DRAFT' },
];

export const useAgentStore = create<AgentState>((set) => ({
  agents: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 400));
    set({ agents: mockAgents, loading: false });
  },
}));
```

- [ ] **Step 2: Rewrite AgentListPage**

```typescript
// src/modules/agent/pages/AgentListPage.tsx
import { useEffect } from 'react';
import { Card, Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAgentStore } from '../stores/agentStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function AgentListPage() {
  const { agents, loading, fetchList } = useAgentStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Agent 管理" actionLabel="新建 Agent" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={agents.length} label="Agent 总数" />
          <StatCard value={agents.filter((a) => a.status === 'ACTIVE').length} label="运行中" />
          <StatCard value="1,247" label="今日会话" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : agents.length === 0 ? (
          <EmptyState icon="🤖" title="暂无 Agent" description="创建你的第一个 AI Agent" actionLabel="新建 Agent" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {agents.map((agent) => (
              <Card
                key={agent.id}
                hoverable
                onClick={() => navigate(`/agent/${agent.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{agent.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {agent.description} · {agent.model} · {agent.coordinationMode}模式 · {agent.skillCount}个Skill · {agent.kbCount}个知识库
                    </div>
                  </div>
                  <StatusTag status={agent.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules/agent && git commit -m "feat: implement agent list page with mock data and stats"
```

---

### Task 10: Skill 模块 — Skill 列表页

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/skill/pages/SkillListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/skill/stores/skillStore.ts`

- [ ] **Step 1: Write skillStore**

```typescript
// src/modules/skill/stores/skillStore.ts
import { create } from 'zustand';

interface SkillDef {
  id: string;
  name: string;
  description: string;
  skillLevel: 'PROMPT' | 'FUNCTION' | 'AGENT';
  status: string;
}

const mockSkills: SkillDef[] = [
  { id: '1', name: 'proposal-section-writer', description: '申报书各章节写作模板', skillLevel: 'PROMPT', status: 'ACTIVE' },
  { id: '2', name: 'proposal-formatter', description: '格式校验（字号/行距/页边距）', skillLevel: 'FUNCTION', status: 'ACTIVE' },
  { id: '3', name: 'budget-table-generator', description: '经费预算表自动计算与生成', skillLevel: 'FUNCTION', status: 'ACTIVE' },
  { id: '4', name: 'proposal-reviewer', description: '模拟评审专家审读草稿', skillLevel: 'AGENT', status: 'DRAFT' },
];

const levelLabel: Record<string, string> = { PROMPT: 'Prompt模板', FUNCTION: '函数', AGENT: '子Agent' };
const levelColor: Record<string, string> = { PROMPT: '#f0f5ff', FUNCTION: '#f0fdf4', AGENT: '#fffcf0' };

interface SkillState {
  skills: SkillDef[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useSkillStore = create<SkillState>((set) => ({
  skills: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ skills: mockSkills, loading: false });
  },
}));
export { levelLabel, levelColor };
```

- [ ] **Step 2: Rewrite SkillListPage with level grouping**

```typescript
// src/modules/skill/pages/SkillListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useSkillStore, levelLabel, levelColor } from '../stores/skillStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function SkillListPage() {
  const { skills, loading, fetchList } = useSkillStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Skill 管理" actionLabel="注册 Skill" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={skills.length} label="Skill 总数" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'PROMPT').length} label="Layer 1 Prompt" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'FUNCTION').length} label="Layer 2 Function" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'AGENT').length} label="Layer 3 Agent" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : skills.length === 0 ? (
          <EmptyState icon="🛠" title="暂无 Skill" description="注册你的第一个 AI Skill" actionLabel="注册 Skill" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {skills.map((skill) => (
              <Card
                key={skill.id}
                hoverable
                onClick={() => navigate(`/skill/${skill.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{skill.name}</span>
                      <Tag style={{ background: levelColor[skill.skillLevel], border: 'none', borderRadius: 6, fontSize: 11, padding: '1px 8px' }}>
                        {levelLabel[skill.skillLevel]}
                      </Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>{skill.description}</div>
                  </div>
                  <StatusTag status={skill.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules/skill && git commit -m "feat: implement skill list page with level grouping and stats"
```

---

### Task 11: MCP + Model 模块列表页

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/mcp/pages/McpServerListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/mcp/stores/mcpStore.ts`
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/model/pages/ModelListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/model/stores/modelStore.ts`

- [ ] **Step 1: Write mcpStore**

```typescript
// src/modules/mcp/stores/mcpStore.ts
import { create } from 'zustand';

interface McpServer {
  id: string;
  name: string;
  serverType: 'BUILTIN' | 'EXTERNAL';
  transport: 'SSE' | 'STREAMABLE_HTTP';
  toolsCount: number;
  healthStatus: string;
  status: string;
}

const mockServers: McpServer[] = [
  { id: '1', name: '文件系统服务', serverType: 'BUILTIN', transport: 'SSE', toolsCount: 8, healthStatus: 'HEALTHY', status: 'ACTIVE' },
  { id: '2', name: '数据库查询服务', serverType: 'BUILTIN', transport: 'STREAMABLE_HTTP', toolsCount: 5, healthStatus: 'HEALTHY', status: 'ACTIVE' },
  { id: '3', name: '外部天气API', serverType: 'EXTERNAL', transport: 'SSE', toolsCount: 3, healthStatus: 'DEGRADED', status: 'ACTIVE' },
];

interface McpState {
  servers: McpServer[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useMcpStore = create<McpState>((set) => ({
  servers: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ servers: mockServers, loading: false });
  },
}));
```

- [ ] **Step 2: Rewrite McpServerListPage**

```typescript
// src/modules/mcp/pages/McpServerListPage.tsx
import { useEffect } from 'react';
import { Card, Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useMcpStore } from '../stores/mcpStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const healthLabel: Record<string, string> = { HEALTHY: '健康', DEGRADED: '降级', UNHEALTHY: '异常', UNKNOWN: '未知' };

export default function McpServerListPage() {
  const { servers, loading, fetchList } = useMcpStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="MCP Server 管理" actionLabel="注册 Server" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={servers.length} label="Server 总数" />
          <StatCard value={servers.filter((s) => s.healthStatus === 'HEALTHY').length} label="健康" />
          <StatCard value={servers.reduce((sum, s) => sum + s.toolsCount, 0)} label="工具总数" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : servers.length === 0 ? (
          <EmptyState icon="🔌" title="暂无 MCP Server" description="注册你的第一个 MCP Server" actionLabel="注册 Server" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {servers.map((srv) => (
              <Card key={srv.id} hoverable onClick={() => navigate(`/mcp/${srv.id}`)} styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{srv.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {srv.serverType} · {srv.transport} · {srv.toolsCount} 工具 · 健康: {healthLabel[srv.healthStatus]}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <StatusTag status={srv.healthStatus} />
                    <StatusTag status={srv.status} />
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Write modelStore**

```typescript
// src/modules/model/stores/modelStore.ts
import { create } from 'zustand';

interface ModelDef {
  id: string;
  name: string;
  provider: string;
  endpoint: string;
  capabilities: string[];
  priority: number;
  status: string;
}

const mockModels: ModelDef[] = [
  { id: '1', name: 'qwen-max', provider: 'DASHSCOPE', endpoint: 'https://dashscope.aliyuncs.com', capabilities: ['chat', 'function_calling'], priority: 0, status: 'ACTIVE' },
  { id: '2', name: 'qwen-plus', provider: 'DASHSCOPE', endpoint: 'https://dashscope.aliyuncs.com', capabilities: ['chat'], priority: 1, status: 'ACTIVE' },
  { id: '3', name: 'deepseek-v3', provider: 'OPENAI', endpoint: 'https://api.deepseek.com', capabilities: ['chat', 'function_calling'], priority: 2, status: 'ACTIVE' },
];

interface ModelState {
  models: ModelDef[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useModelStore = create<ModelState>((set) => ({
  models: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ models: mockModels, loading: false });
  },
}));
```

- [ ] **Step 4: Rewrite ModelListPage**

```typescript
// src/modules/model/pages/ModelListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useModelStore } from '../stores/modelStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function ModelListPage() {
  const { models, loading, fetchList } = useModelStore();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="模型管理" actionLabel="添加模型" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={models.length} label="模型总数" />
          <StatCard value={models.filter((m) => m.status === 'ACTIVE').length} label="启用中" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : models.length === 0 ? (
          <EmptyState icon="🧠" title="暂无模型" description="添加你的第一个 AI 模型" actionLabel="添加模型" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {models.map((m) => (
              <Card key={m.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{m.name}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{m.provider}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {m.endpoint} · 优先级: {m.priority} · {m.capabilities.join(', ')}
                    </div>
                  </div>
                  <StatusTag status={m.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 5: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules/mcp ai-base-frontend/src/modules/model && git commit -m "feat: implement MCP server and model gateway list pages with mock data"
```

---

### Task 12: Eval + Platform + 剩余占位页面替换

**Files:**
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/eval/pages/EvalDatasetListPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/eval/stores/evalStore.ts`
- Modify: `D:/work/datang/AIBase/ai-base-frontend/src/modules/platform/pages/PromptVersionPage.tsx`
- Create: `D:/work/datang/AIBase/ai-base-frontend/src/modules/platform/stores/platformStore.ts`
- Modify remaining placeholder pages to use EmptyState

- [ ] **Step 1: Write evalStore + EvalDatasetListPage**

```typescript
// src/modules/eval/stores/evalStore.ts
import { create } from 'zustand';

interface EvalDataset {
  id: string;
  name: string;
  evalType: 'RAG' | 'AGENT';
  itemCount: number;
  createdAt: string;
}

const mockDatasets: EvalDataset[] = [
  { id: '1', name: '申报书质量评估集', evalType: 'RAG', itemCount: 50, createdAt: '2026-05-20' },
  { id: '2', name: 'Agent任务成功率测试集', evalType: 'AGENT', itemCount: 30, createdAt: '2026-05-25' },
];

interface EvalState {
  datasets: EvalDataset[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useEvalStore = create<EvalState>((set) => ({
  datasets: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ datasets: mockDatasets, loading: false });
  },
}));
```

- [ ] **Step 2: Rewrite EvalDatasetListPage**

```typescript
// src/modules/eval/pages/EvalDatasetListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useEvalStore } from '../stores/evalStore';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function EvalDatasetListPage() {
  const { datasets, loading, fetchList } = useEvalStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="评估数据集" actionLabel="新建数据集" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={datasets.length} label="数据集总数" />
          <StatCard value={datasets.reduce((s, d) => s + d.itemCount, 0)} label="评估条目" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : datasets.length === 0 ? (
          <EmptyState icon="📊" title="暂无评估数据集" description="创建评估数据集来衡量 AI 质量" actionLabel="新建数据集" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {datasets.map((ds) => (
              <Card key={ds.id} hoverable onClick={() => navigate(`/eval/results/${ds.id}`)} styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{ds.name}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{ds.evalType}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>{ds.itemCount} 条评估项 · 创建于 {ds.createdAt}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Write platformStore + PromptVersionPage**

```typescript
// src/modules/platform/stores/platformStore.ts
import { create } from 'zustand';

interface PromptVersion {
  id: string;
  refType: string;
  refId: string;
  version: number;
  status: string;
  createdAt: string;
  createdBy: string;
}

const mockPrompts: PromptVersion[] = [
  { id: '1', refType: 'AGENT', refId: 'agent-1', version: 3, status: 'PUBLISHED', createdAt: '2026-06-01', createdBy: 'dev-user' },
  { id: '2', refType: 'SKILL', refId: 'skill-1', version: 2, status: 'DRAFT', createdAt: '2026-06-01', createdBy: 'dev-user' },
];

interface PlatformState {
  prompts: PromptVersion[];
  loading: boolean;
  fetchPrompts: () => Promise<void>;
}

export const usePlatformStore = create<PlatformState>((set) => ({
  prompts: [],
  loading: false,
  fetchPrompts: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ prompts: mockPrompts, loading: false });
  },
}));
```

- [ ] **Step 4: Rewrite PromptVersionPage**

```typescript
// src/modules/platform/pages/PromptVersionPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { usePlatformStore } from '../stores/platformStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const refLabel: Record<string, string> = { AGENT: 'Agent', SKILL: 'Skill', WORKFLOW: 'Workflow' };

export default function PromptVersionPage() {
  const { prompts, loading, fetchPrompts } = usePlatformStore();

  useEffect(() => { fetchPrompts(); }, [fetchPrompts]);

  return (
    <div>
      <PageHeader title="Prompt 版本管理">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={prompts.length} label="Prompt 总数" />
          <StatCard value={prompts.filter((p) => p.status === 'PUBLISHED').length} label="已发布" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : prompts.length === 0 ? (
          <EmptyState icon="📋" title="暂无 Prompt 版本" description="Prompt 版本将在创建 Agent/Skill 时自动生成" />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {prompts.map((p) => (
              <Card key={p.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{refLabel[p.refType]} Prompt v{p.version}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{p.refType}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>ref: {p.refId} · {p.createdAt} · by {p.createdBy}</div>
                  </div>
                  <StatusTag status={p.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 5: Replace remaining placeholder pages with EmptyState**

Update the following pages to use the EmptyState component instead of inline placeholders:

```
src/modules/workflow/pages/WorkflowEditorPage.tsx → EmptyState icon="⚙️" title="工作流编辑器" description="可视化编辑 DAG 工作流"
src/modules/workflow/pages/WorkflowInstancePage.tsx → EmptyState icon="⚙️" title="工作流实例" description="查看工作流执行状态"
src/modules/knowledge/pages/KbDetailPage.tsx → EmptyState icon="📚" title="知识库详情" description="管理连接器和搜索引擎配置"
src/modules/knowledge/pages/DocumentUploadPage.tsx → EmptyState icon="📚" title="文档上传" description="上传文档到知识库"
src/modules/agent/pages/AgentDetailPage.tsx → EmptyState icon="🤖" title="Agent 详情" description="配置 Agent 的模型、Prompt、Skill 和知识库"
src/modules/agent/pages/SessionHistoryPage.tsx → EmptyState icon="🤖" title="会话记录" description="查看所有 Agent 会话历史"
src/modules/skill/pages/SkillDetailPage.tsx → EmptyState icon="🛠" title="Skill 详情" description="查看 Skill 定义和参数 Schema"
src/modules/skill/pages/SkillVersionHistory.tsx → EmptyState icon="🛠" title="版本历史" description="查看 Skill 版本变更记录"
src/modules/mcp/pages/McpServerDetailPage.tsx → EmptyState icon="🔌" title="Server 详情" description="查看 MCP Server 连接日志"
src/modules/mcp/pages/McpToolListPage.tsx → EmptyState icon="🔌" title="工具列表" description="浏览所有已注册的 MCP 工具"
src/modules/model/pages/ModelRouteRulePage.tsx → EmptyState icon="🧠" title="路由规则" description="配置模型路由和降级策略"
src/modules/model/pages/ModelCallLogPage.tsx → EmptyState icon="🧠" title="调用日志" description="查看模型调用记录和成本统计"
src/modules/eval/pages/EvalTaskListPage.tsx → EmptyState icon="📊" title="评估任务" description="查看评估任务执行结果"
src/modules/eval/pages/EvalResultPage.tsx → EmptyState icon="📊" title="评估结果" description="查看详细评估指标和标注"
src/modules/platform/pages/ApprovalPage.tsx → EmptyState icon="📋" title="审批工作台" description="处理 Human-in-the-Loop 审批任务"
```

Each follows this pattern:
```typescript
import EmptyState from '../../../shared/components/EmptyState';
export default function SomePage() {
  return <EmptyState icon="📚" title="页面名称" description="功能描述" />;
}
```

- [ ] **Step 6: Commit**

```bash
cd D:/work/datang/AIBase && git add ai-base-frontend/src/modules && git commit -m "feat: implement eval and platform list pages, replace remaining placeholders with EmptyState"
```

---

### Task 13: 最终验证

- [ ] **Step 1: Run full type check**

```bash
cd D:/work/datang/AIBase/ai-base-frontend && npx tsc --noEmit
```
Expected: No type errors.

- [ ] **Step 2: Run dev build**

```bash
cd D:/work/datang/AIBase/ai-base-frontend && npm run build
```
Expected: Vite builds successfully, outputs to `dist/`.

- [ ] **Step 3: Manual smoke test**

Run: `npm run dev`

Verify:
- `/` → Chat page with session list + empty state + create session flow
- `/knowledge` → Knowledge list with stats + create modal (calls real backend)
- `/agent` → Agent list with mock data
- `/skill` → Skill list with level grouping
- `/mcp` → MCP server list with health status
- `/model` → Model list with provider tags
- `/eval` → Dataset list
- `/platform/prompts` → Prompt version list
- All other detail/edit pages → EmptyState placeholders
- Top nav switching works, sidebar highlights correctly
- Theme colors correct (primary #597ef7, card border-radius 12px)

- [ ] **Step 4: Verify proxy to backend**

With backend running on 8080: navigate to `/knowledge`, click "新建知识库", fill form, submit.
Expected: `POST /api/v1/knowledge/kb` returns 200, new KB appears in list.

- [ ] **Step 5: Commit**

```bash
cd D:/work/datang/AIBase && git add -A && git commit -m "chore: final verification fixes and polish"
```

---

## 验证命令总览

```bash
# Install
cd D:/work/datang/AIBase/ai-base-frontend && npm install

# Type check
npx tsc --noEmit

# Dev server (with API proxy to backend)
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

## 项目完成标准

- [x] Vite + React + TS 项目可启动
- [x] Ant Design 5 theme tokens 生效
- [x] 24 个路由全部懒加载，无 404
- [x] 顶部 8 项导航，分隔线区分业务/管理
- [x] Chat 首页：会话列表 + 消息气泡 + 输入发送
- [x] Knowledge 列表：真实后端 CRUD（createKb / listKb）
- [x] **全部 8 个模块 store 迁移至真实 API 调用** (2026-06-04)
- [x] **17 个页面字段对齐后端 API 实体类型** (2026-06-04)
- [x] 所有详情/编辑页面：EmptyState 占位（等待后端接口完善）
- [x] 共享组件：StatCard, StatusTag, EmptyState, PageHeader
- [x] API 客户端：Axios 拦截器注入 X-User-Id/X-Dept-Id
- [x] 8 个 API 模块全部创建 (agent/skill/model/mcp/eval/platform/workflow/knowledge)
- [x] TypeScript 编译零错误
- [x] Vite 生产构建成功
