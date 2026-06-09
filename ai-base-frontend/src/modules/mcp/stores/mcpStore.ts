import { create } from 'zustand';
import { listMcpServers, getMcpServerById, listAllMcpTools, createMcpServer, updateMcpServer, deleteMcpServer, type McpServer, type McpTool, type McpCreateRequest } from '../../../shared/api/mcp';

interface McpState {
  servers: McpServer[];
  loading: boolean;
  detail: McpServer | null;
  tools: McpTool[];
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  fetchAllTools: () => Promise<void>;
  create: (data: McpCreateRequest) => Promise<McpServer | null>;
  update: (id: string, data: Partial<McpCreateRequest>) => Promise<McpServer | null>;
  remove: (id: string) => Promise<boolean>;
}

export const useMcpStore = create<McpState>((set) => ({
  servers: [],
  loading: false,
  detail: null,
  tools: [],
  fetchList: async () => {
    set({ loading: true });
    const res = await listMcpServers();
    set({ servers: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchDetail: async (id: string) => {
    set({ loading: true });
    const res = await getMcpServerById(id);
    set({ detail: res.success ? (res.data ?? null) : null, loading: false });
  },
  fetchAllTools: async () => {
    set({ loading: true });
    const res = await listAllMcpTools();
    set({ tools: res.success ? (res.data ?? []) : [], loading: false });
  },
  create: async (data: McpCreateRequest) => {
    set({ loading: true });
    const res = await createMcpServer(data);
    if (res.success) {
      set((s) => ({ servers: [...s.servers, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  update: async (id: string, data: Partial<McpCreateRequest>) => {
    set({ loading: true });
    const res = await updateMcpServer(id, data);
    if (res.success) {
      set((s) => ({
        servers: s.servers.map((srv) => (srv.id === id ? { ...srv, ...res.data } : srv)),
        detail: s.detail?.id === id ? { ...s.detail, ...res.data } : s.detail,
        loading: false,
      }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  remove: async (id: string) => {
    set({ loading: true });
    const res = await deleteMcpServer(id);
    if (res.success) {
      set((s) => ({ servers: s.servers.filter((srv) => srv.id !== id), loading: false }));
      return true;
    }
    set({ loading: false });
    return false;
  },
}));
