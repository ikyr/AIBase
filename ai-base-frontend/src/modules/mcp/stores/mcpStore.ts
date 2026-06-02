// src/modules/mcp/stores/mcpStore.ts
import { create } from 'zustand';

interface McpServer {
  id: string;
  name: string;
  serverType: 'BUILTIN' | 'EXTERNAL';
  transport: 'SSE' | 'STREAMABLE_HTTP';
  toolsCount: number;
  healthStatus: string;
  status: string;
}

const mockServers: McpServer[] = [
  { id: '1', name: '文件系统服务', serverType: 'BUILTIN', transport: 'SSE', toolsCount: 8, healthStatus: 'HEALTHY', status: 'ACTIVE' },
  { id: '2', name: '数据库查询服务', serverType: 'BUILTIN', transport: 'STREAMABLE_HTTP', toolsCount: 5, healthStatus: 'HEALTHY', status: 'ACTIVE' },
  { id: '3', name: '外部天气API', serverType: 'EXTERNAL', transport: 'SSE', toolsCount: 3, healthStatus: 'DEGRADED', status: 'ACTIVE' },
];

interface McpState {
  servers: McpServer[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useMcpStore = create<McpState>((set) => ({
  servers: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ servers: mockServers, loading: false });
  },
}));
