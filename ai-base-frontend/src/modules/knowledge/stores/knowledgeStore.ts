import { create } from 'zustand';
import { listKb, createKb, searchKb, type KbConfigInfo, type KbCreateRequest, type SearchResults } from '../../../shared/api/knowledge';

interface KnowledgeState {
  kbs: KbConfigInfo[];
  loading: boolean;
  error: string | null;
  detail: KbConfigInfo | null;
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  create: (req: KbCreateRequest) => Promise<void>;
  // Search
  searchResults: SearchResults;
  searching: boolean;
  searchError: string | null;
  search: (kbId: string, query: string, topK?: number, strategy?: 'VECTOR' | 'KEYWORD' | 'HYBRID') => Promise<void>;
  clearSearch: () => void;
}

export const useKnowledgeStore = create<KnowledgeState>((set) => ({
  kbs: [],
  loading: false,
  error: null,
  detail: null,
  searchResults: [],
  searching: false,
  searchError: null,

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

  search: async (kbId: string, query: string, topK = 10, strategy = 'HYBRID') => {
    set({ searching: true, searchError: null });
    const res = await searchKb({ kbId, query, topK, strategy });
    if (res.success) {
      set({ searchResults: res.data ?? [], searching: false });
    } else {
      set({ searchResults: [], searching: false, searchError: res.error ?? '搜索失败' });
    }
  },

  clearSearch: () => set({ searchResults: [], searchError: null }),
}));
