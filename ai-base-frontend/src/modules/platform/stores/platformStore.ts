// src/modules/platform/stores/platformStore.ts
import { create } from 'zustand';

interface PromptVersion {
  id: string;
  refType: string;
  refId: string;
  version: number;
  status: string;
  createdAt: string;
  createdBy: string;
}

const mockPrompts: PromptVersion[] = [
  { id: '1', refType: 'AGENT', refId: 'agent-1', version: 3, status: 'PUBLISHED', createdAt: '2026-06-01', createdBy: 'dev-user' },
  { id: '2', refType: 'SKILL', refId: 'skill-1', version: 2, status: 'DRAFT', createdAt: '2026-06-01', createdBy: 'dev-user' },
];

interface PlatformState {
  prompts: PromptVersion[];
  loading: boolean;
  fetchPrompts: () => Promise<void>;
}

export const usePlatformStore = create<PlatformState>((set) => ({
  prompts: [],
  loading: false,
  fetchPrompts: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ prompts: mockPrompts, loading: false });
  },
}));
