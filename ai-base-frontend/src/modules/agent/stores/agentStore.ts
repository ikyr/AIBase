import { create } from 'zustand';
import { listAgents, getAgentById, listAgentSessions, getAgentMessages, createAgent, updateAgent, deleteAgent, type AgentDef, type AgentSession, type AgentMessage, type AgentCreateRequest } from '../../../shared/api/agent';

interface AgentState {
  agents: AgentDef[];
  loading: boolean;
  error: string | null;
  detail: AgentDef | null;
  sessions: AgentSession[];
  messages: AgentMessage[];
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  fetchSessions: () => Promise<void>;
  fetchMessages: (sessionId: string) => Promise<void>;
  create: (data: AgentCreateRequest) => Promise<AgentDef | null>;
  update: (id: string, data: Partial<AgentCreateRequest>) => Promise<AgentDef | null>;
  remove: (id: string) => Promise<boolean>;
}

export const useAgentStore = create<AgentState>((set) => ({
  agents: [],
  loading: false,
  error: null,
  detail: null,
  sessions: [],
  messages: [],
  fetchList: async () => {
    set({ loading: true, error: null });
    const res = await listAgents();
    set({ agents: res.success ? (res.data ?? []) : [], loading: false, error: res.error ?? null });
  },
  fetchDetail: async (id: string) => {
    set({ loading: true });
    const res = await getAgentById(id);
    set({ detail: res.success ? (res.data ?? null) : null, loading: false });
  },
  fetchSessions: async () => {
    set({ loading: true });
    const res = await listAgentSessions();
    set({ sessions: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchMessages: async (sessionId: string) => {
    set({ loading: true });
    const res = await getAgentMessages(sessionId);
    set({ messages: res.success ? (res.data ?? []) : [], loading: false });
  },
  create: async (data: AgentCreateRequest) => {
    set({ loading: true });
    const res = await createAgent(data);
    if (res.success) {
      set((s) => ({ agents: [...s.agents, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false, error: res.error });
    return null;
  },
  update: async (id: string, data: Partial<AgentCreateRequest>) => {
    set({ loading: true });
    const res = await updateAgent(id, data);
    if (res.success) {
      set((s) => ({
        agents: s.agents.map((a) => (a.id === id ? { ...a, ...res.data } : a)),
        detail: s.detail?.id === id ? { ...s.detail, ...res.data } : s.detail,
        loading: false,
      }));
      return res.data;
    }
    set({ loading: false, error: res.error });
    return null;
  },
  remove: async (id: string) => {
    set({ loading: true });
    const res = await deleteAgent(id);
    if (res.success) {
      set((s) => ({ agents: s.agents.filter((a) => a.id !== id), loading: false }));
      return true;
    }
    set({ loading: false, error: res.error });
    return false;
  },
}));
