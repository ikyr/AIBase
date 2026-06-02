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
