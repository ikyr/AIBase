import { create } from 'zustand';
import { listEvalDatasets, listEvalTasks, getEvalResults, type EvalDataset, type EvalTask, type EvalResult } from '../../../shared/api/eval';

interface EvalState {
  datasets: EvalDataset[];
  tasks: EvalTask[];
  results: EvalResult[];
  loading: boolean;
  error: string | null;
  fetchList: () => Promise<void>;
  fetchTasks: () => Promise<void>;
  fetchResults: (taskId: string) => Promise<void>;
}

export const useEvalStore = create<EvalState>((set) => ({
  datasets: [],
  tasks: [],
  results: [],
  loading: false,
  error: null,
  fetchList: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listEvalDatasets();
      if (!res.success) throw new Error(res.error || 'Failed to fetch datasets');
      set({ datasets: res.data ?? [], loading: false });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
  fetchTasks: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listEvalTasks();
      if (!res.success) throw new Error(res.error || 'Failed to fetch tasks');
      set({ tasks: res.data ?? [], loading: false });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
  fetchResults: async (taskId: string) => {
    set({ loading: true, error: null });
    try {
      const res = await getEvalResults(taskId);
      if (!res.success) throw new Error(res.error || 'Failed to fetch results');
      set({ results: res.data ?? [], loading: false });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
}));
