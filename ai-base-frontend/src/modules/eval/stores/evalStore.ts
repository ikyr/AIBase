import { create } from 'zustand';
import { listEvalDatasets, listEvalTasks, getEvalResults, type EvalDataset, type EvalTask, type EvalResult } from '../../../shared/api/eval';

interface EvalState {
  datasets: EvalDataset[];
  tasks: EvalTask[];
  loading: boolean;
  results: EvalResult[];
  fetchList: () => Promise<void>;
  fetchTasks: () => Promise<void>;
  fetchResults: (taskId: string) => Promise<void>;
}

export const useEvalStore = create<EvalState>((set) => ({
  datasets: [],
  tasks: [],
  loading: false,
  results: [],
  fetchList: async () => {
    set({ loading: true });
    const res = await listEvalDatasets();
    set({ datasets: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchTasks: async () => {
    set({ loading: true });
    const res = await listEvalTasks();
    set({ tasks: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchResults: async (taskId: string) => {
    set({ loading: true });
    const res = await getEvalResults(taskId);
    set({ results: res.success ? (res.data ?? []) : [], loading: false });
  },
}));
