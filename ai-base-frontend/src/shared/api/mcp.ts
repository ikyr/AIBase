import { get, post, del, type ApiResponse } from './client';

export interface McpCreateRequest {
  name: string;
  description?: string;
  serverType?: string;
  transport?: string;
  endpoint?: string;
  authConfig?: string;
}

export interface McpServer {
  id: string;
  name: string;
  serverType: string;
  transport: string;
  endpoint: string;
  description: string;
  healthStatus: string;
  status: string;
  createdAt: string;
}

export interface McpTool {
  id: string;
  serverId: string;
  name: string;
  description: string;
  inputSchema: string;
  status: string;
}

export function listMcpServers(): Promise<ApiResponse<McpServer[]>> {
  return get<McpServer[]>('/mcp/servers');
}

export function getMcpServerById(id: string): Promise<ApiResponse<McpServer>> {
  return get<McpServer>(`/mcp/servers/${id}`);
}

export function listMcpServerTools(serverId: string): Promise<ApiResponse<McpTool[]>> {
  return get<McpTool[]>(`/mcp/servers/${serverId}/tools`);
}

export function listAllMcpTools(): Promise<ApiResponse<McpTool[]>> {
  return get<McpTool[]>('/mcp/tools');
}

export function createMcpServer(data: McpCreateRequest): Promise<ApiResponse<McpServer>> {
  return post<McpServer>('/mcp/servers', data);
}

export function updateMcpServer(id: string, data: Partial<McpCreateRequest>): Promise<ApiResponse<McpServer>> {
  return post<McpServer>(`/mcp/servers/${id}`, data);
}

export function deleteMcpServer(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/mcp/servers/${id}`);
}
