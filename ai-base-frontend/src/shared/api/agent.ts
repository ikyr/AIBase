import { get, post, del, type ApiResponse } from './client';

export interface AgentCreateRequest {
  name: string;
  description?: string;
  systemPrompt?: string;
  model?: string;
  tools?: string;
  skillIds?: string;
  kbIds?: string;
  coordinationMode?: string;
  constraints?: string;
}

export interface AgentDef {
  id: string;
  name: string;
  description: string;
  systemPrompt: string;
  model: string;
  tools: string;
  skillIds: string;
  kbIds: string;
  coordinationMode: string;
  constraints: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface AgentSession {
  id: string;
  agentId: string;
  userId: string;
  title: string;
  status: string;
  context: string;
  traceId: string;
  startedAt: string;
  completedAt: string;
}

export interface AgentMessage {
  id: string;
  sessionId: string;
  parentId: string;
  role: string;
  content: string;
  contentType: string;
  attachments: string;
  toolCalls: string;
  tokenCount: number;
}

export function listAgents(): Promise<ApiResponse<AgentDef[]>> {
  return get<AgentDef[]>('/agent');
}

export function getAgentById(id: string): Promise<ApiResponse<AgentDef>> {
  return get<AgentDef>(`/agent/${id}`);
}

export function listAgentSessions(): Promise<ApiResponse<AgentSession[]>> {
  return get<AgentSession[]>('/agent/sessions');
}

export function getAgentMessages(sessionId: string): Promise<ApiResponse<AgentMessage[]>> {
  return get<AgentMessage[]>(`/agent/sessions/${sessionId}/messages`);
}

export function createAgent(data: AgentCreateRequest): Promise<ApiResponse<AgentDef>> {
  return post<AgentDef>('/agent', data);
}

export function updateAgent(id: string, data: Partial<AgentCreateRequest>): Promise<ApiResponse<AgentDef>> {
  return post<AgentDef>(`/agent/${id}`, data);
}

export function deleteAgent(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/agent/${id}`);
}

export function createAgentSession(agentId: string): Promise<ApiResponse<AgentSession>> {
  return post<AgentSession>(`/agent/${agentId}/sessions`);
}
