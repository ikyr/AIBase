// src/modules/knowledge/stores/knowledgeStore.ts
import { create } from 'zustand';
import { listKb, createKb } from '../../../shared/api/knowledge';
import type { KbConfigInfo, KbCreateRequest } from '../../../shared/api/types';

interface KnowledgeState {
  kbs: KbConfigInfo[];
  loading: boolean;
  error: string | null;
  fetchList: () => Promise<void>;
  create: (req: KbCreateRequest) => Promise<void>;
}

export const useKnowledgeStore = create<KnowledgeState>((set) => ({
  kbs: [],
  loading: false,
  error: null,

  fetchList: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listKb();
      if (res.success && res.data) {
        set({ kbs: res.data, loading: false });
      } else {
        set({ error: res.error || 'Failed to load', loading: false });
      }
    } catch {
      set({ error: 'Network error', loading: false });
    }
  },

  create: async (req: KbCreateRequest) => {
    set({ loading: true, error: null });
    try {
      const res = await createKb(req);
      if (res.success) {
        // Refresh list
        const listRes = await listKb();
        if (listRes.success && listRes.data) {
          set({ kbs: listRes.data, loading: false });
        }
      } else {
        set({ error: res.error || 'Failed to create', loading: false });
      }
    } catch {
      set({ error: 'Network error', loading: false });
    }
  },
}));
