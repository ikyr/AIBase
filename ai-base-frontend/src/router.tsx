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

interface LazyProps {
  children: React.ReactNode;
}

function Lazy({ children }: LazyProps) {
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
