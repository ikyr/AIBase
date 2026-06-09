import { create } from 'zustand';
import { listKb, createKb, type KbConfigInfo, type KbCreateRequest } from '../../../shared/api/knowledge';

interface KnowledgeState {
  kbs: KbConfigInfo[];
  loading: boolean;
  error: string | null;
  detail: KbConfigInfo | null;
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  create: (req: KbCreateRequest) => Promise<void>;
}

export const useKnowledgeStore = create<KnowledgeState>((set) => ({
  kbs: [],
  loading: false,
  error: null,
  detail: null,
  fetchList: async () => {
    set({ loading: true, error: null });
    const res = await listKb();
    set({ kbs: res.success ? (res.data ?? []) : [], loading: false, error: res.error ?? null });
  },
  fetchDetail: async (id: string) => {
    set({ loading: true });
    const res = await listKb();
    if (res.success && res.data) {
      set({ detail: res.data.find((k) => k.id === id) ?? null, loading: false });
    } else {
      set({ detail: null, loading: false });
    }
  },
  create: async (req: KbCreateRequest) => {
    set({ loading: true, error: null });
    const res = await createKb(req);
    if (res.success && res.data) {
      set((s) => ({ kbs: [...s.kbs, res.data!], loading: false }));
    } else {
      set({ loading: false, error: res.error ?? '创建失败' });
    }
  },
}));
