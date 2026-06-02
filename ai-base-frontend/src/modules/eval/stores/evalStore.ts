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
