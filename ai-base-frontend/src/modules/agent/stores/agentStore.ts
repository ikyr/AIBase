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
