import { create } from 'zustand';
import { listModels, listRouteRules, listCallLogs, createModel, updateModel, deleteModel, createRouteRule, deleteRouteRule, type ModelConfig, type ModelRouteRule, type ModelCallLog, type ModelCreateRequest, type RouteRuleCreateRequest } from '../../../shared/api/model';

interface ModelState {
  models: ModelConfig[];
  loading: boolean;
  rules: ModelRouteRule[];
  logs: ModelCallLog[];
  fetchList: () => Promise<void>;
  fetchRules: () => Promise<void>;
  fetchLogs: () => Promise<void>;
  create: (data: ModelCreateRequest) => Promise<ModelConfig | null>;
  update: (id: string, data: Partial<ModelCreateRequest>) => Promise<ModelConfig | null>;
  remove: (id: string) => Promise<boolean>;
  createRule: (data: RouteRuleCreateRequest) => Promise<ModelRouteRule | null>;
  removeRule: (id: string) => Promise<boolean>;
}

export const useModelStore = create<ModelState>((set) => ({
  models: [],
  loading: false,
  rules: [],
  logs: [],
  fetchList: async () => {
    set({ loading: true });
    const res = await listModels();
    set({ models: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchRules: async () => {
    set({ loading: true });
    const res = await listRouteRules();
    set({ rules: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchLogs: async () => {
    set({ loading: true });
    const res = await listCallLogs();
    set({ logs: res.success ? (res.data ?? []) : [], loading: false });
  },
  create: async (data: ModelCreateRequest) => {
    set({ loading: true });
    const res = await createModel(data);
    if (res.success) {
      set((s) => ({ models: [...s.models, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  update: async (id: string, data: Partial<ModelCreateRequest>) => {
    set({ loading: true });
    const res = await updateModel(id, data);
    if (res.success) {
      set((s) => ({
        models: s.models.map((m) => (m.id === id ? { ...m, ...res.data } : m)),
        loading: false,
      }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  remove: async (id: string) => {
    set({ loading: true });
    const res = await deleteModel(id);
    if (res.success) {
      set((s) => ({ models: s.models.filter((m) => m.id !== id), loading: false }));
      return true;
    }
    set({ loading: false });
    return false;
  },
  createRule: async (data: RouteRuleCreateRequest) => {
    set({ loading: true });
    const res = await createRouteRule(data);
    if (res.success && res.data) {
      set((s) => ({ rules: [...s.rules, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  removeRule: async (id: string) => {
    set({ loading: true });
    const res = await deleteRouteRule(id);
    if (res.success) {
      set((s) => ({ rules: s.rules.filter((r) => r.id !== id), loading: false }));
      return true;
    }
    set({ loading: false });
    return false;
  },
}));
