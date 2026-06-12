# AIBase vs Dify 功能对比分析

> 更新日期：2026-06-12

## 概述

Dify 是当前最流行的开源 LLM 应用开发平台（GitHub 40K+ Stars），AIBase 是企业级 AI 底座平台。本文档系统对比两者功能，明确 AIBase 的定位、差距和后续建设方向。

---

## 一、AIBase 已具备的能力

| 功能领域 | AIBase 实现 | Dify 实现 |
|---------|-----------|----------|
| 工作流编排 | React Flow DAG 编辑器，11 种节点 | 拖拽式，12+ 节点 |
| 知识库 RAG | Milvus 向量检索 + PostgreSQL 关键词检索 + RRF 混合融合 | 多向量库支持 |
| Agent 引擎 | ReAct Loop / Graph Bridge / Negotiation 三模式协调 | ReAct / Function Calling |
| 多模型接入 | DashScope / OpenAI / 本地模型，智能路由 + Fallback 降级 | 100+ 模型 |
| Skill 体系 | Prompt 模板 / Function 函数 / Agent 子代理 三层体系 | 工具 + 插件 |
| MCP 协议 | Client 连接池管理 + Server 工具导出，SSE/Streamable HTTP 双协议 | **不支持** |
| 评估框架 | 数据集/任务/结果/标注，MRR/NDCG/SuccessRate 指标 | 部分支持 |
| Prompt 版本 | 创建/发布/回滚，关联 Agent/Skill/Workflow | 版本控制 + A/B 测试 |
| API 网关 | 鉴权(ApiKey)/限流(TokenBucket)/CORS/日志/TraceID | 认证 |
| 可观测性 | OpenTelemetry + Tempo + Loki + Grafana 全链路 | Langfuse/Opik |
| 微服务架构 | 9 个独立服务 + Gateway，可独立扩缩容 | 单体架构 |
| 审批工作流 | 多步审批链 + SLA 超时升级 + 转派 | **不支持** |
| 模型成本追踪 | 按模型/Agent 维度统计 Token 消耗 | 部分支持 |

---

## 二、AIBase 缺失的核心能力

### P0 — 决定性差距

| 功能 | Dify | AIBase | 复杂度 |
|------|------|--------|--------|
| **Chatflow 对话流引擎** | 智能对话路由、记忆、分支、多轮跳转 | 仅单线 ReAct | HIGH |
| **多格式文档解析** | PDF/PPT/DOCX/Markdown/Notion/网页抓取 | 仅 TXT/MD | MEDIUM |
| **可视化 Prompt IDE** | 在线编辑器、变量注入、A/B 测试 | 仅 JSON 表单 | HIGH |
| **多模态知识库** | 图片+文本统一检索 | 仅文本 | HIGH |

### P1 — 重要差距

| 功能 | Dify | AIBase | 复杂度 |
|------|------|--------|--------|
| **内置工具市场** | 50+ 工具（搜索/画图/抓取...） | 仅 5 个 Tool | MEDIUM |
| **Python 代码沙箱** | Python + Node.js | 仅 GraalJS | MEDIUM |
| **OAuth/SSO + 多租户** | OAuth 2.0 + 企业 SSO + 空间 | 仅 API Key | HIGH |
| **单节点调试** | 每个节点可单独测试 | 无 | MEDIUM |
| **执行回放** | 完整 Trace 回放 + 变量检查 | 仅 Trace 记录 | MEDIUM |
| **用户反馈收集** | 点赞/点踩 + 标注 | 无 | LOW |
| **插件市场** | 20+ 品类，标准化接口 | 无 | HIGH |

### P2 — 补充能力

| 功能 | Dify | AIBase | 复杂度 |
|------|------|--------|--------|
| **多向量库适配** | 12+ 向量数据库统一接口 | 仅 Milvus | MEDIUM |
| **Reranking 重排序** | Cohere/BGE Reranker | 无 | LOW |
| **问题分类器节点** | 自动意图识别+路由 | 仅 CONDITION | LOW |
| **变量赋值器节点** | 节点间类型转换 | 无 | LOW |
| **HTTP 请求节点** | 工作流中调用外部 API | 仅 Agent Tool | LOW |
| **应用模板** | Chatbot/Agent/TextGen 预置 | 无 | LOW |
| **BaaS 自动 API** | 每个应用自动生成 API | 手动 Feign | MEDIUM |
| **微调工具链** | LoRA/QLoRA 可视化训练 | 无 | HIGH |
| **异常检测** | ML 驱动 + 自动熔断 | 仅 MCP 断路器 | MEDIUM |
| **引用溯源** | 自动标注来源文档 | 无 | LOW |
| **DSL 导入导出** | 工作流可移植 | 无 | LOW |

---

## 三、AIBase 独有的差异化优势

| 能力 | 说明 | Dify 有无 |
|------|------|----------|
| **MCP 协议原生支持** | Client + Server 双侧实现，AI 工具互通标准 | 无 |
| **审批工作流 (HITL)** | 多步审批链 + SLA 超时升级，企业合规必需 | 无 |
| **微服务架构** | 独立部署/扩缩容，高可用 | 单体 |
| **Skill 三层体系** | Prompt→Function→Agent 渐进式 | 无分层 |
| **Agent 协商模式** | 多 Agent 辩论/投票/仲裁 | 无 |
| **知识库连接器框架** | DataSourceConnector 接口可扩展 | 内置固定 |
| **完整评估体系** | MRR/NDCG/Precision/Recall/SuccessRate/ToolCallAccuracy | 基础 |

---

## 四、定位建议

AIBase 不应复制 Dify，而应聚焦差异化：

1. **能力底座而非应用工厂** — 为 9 个企业场景提供统一 AI 能力，不追求"人人可建"
2. **MCP 生态先发优势** — 持续投入 MCP 协议，抢占 AI Agent 互通标准
3. **企业合规 + HITL** — 审批流程、SLA、审计日志是政企刚需
4. **可扩展架构** — 微服务 + 插件化接口，支持第三方扩展

---

## 五、建设优先级

```
P0: 多格式文档解析 → Chatflow 对话流 → 可视化 Prompt IDE
P1: 工具市场/插件 → OAuth/SSO → Python 沙箱 → 单节点调试
P2: 多向量库 → 应用模板 → DSL 导入导出 → 用户反馈闭环
```
