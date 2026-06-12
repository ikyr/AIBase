import { get, post, del, type ApiResponse } from './client';

export interface SkillCreateRequest {
  name: string;
  description?: string;
  tags?: string;
  skillLevel?: string;
  promptTemplate?: string;
  params?: string;
  inputSchema?: string;
  outputSchema?: string;
  executionMode?: string;
  timeoutMs?: number;
  agentRefId?: string;
}

export interface SkillDef {
  id: string;
  name: string;
  description: string;
  tags: string;
  skillLevel: string;
  promptTemplate: string;
  params: string;
  inputSchema: string;
  outputSchema: string;
  executionMode: string;
  timeoutMs: number;
  agentRefId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface SkillVersion {
  id: string;
  skillId: string;
  version: string;
  changelog: string;
  definition: string;
  isLatest: boolean;
  status: string;
  createdAt: string;
}

export interface SkillExecutionLog {
  id: string;
  skillId: string;
  skillVersion: string;
  sessionId: string;
  input: string;
  output: string;
  status: string;
  durationMs: number;
  errorMsg: string;
  traceId: string;
  createdAt: string;
}

export function listSkills(): Promise<ApiResponse<SkillDef[]>> {
  return get<SkillDef[]>('/skill');
}

export function getSkillById(id: string): Promise<ApiResponse<SkillDef>> {
  return get<SkillDef>(`/skill/${id}`);
}

export function getSkillVersions(id: string): Promise<ApiResponse<SkillVersion[]>> {
  return get<SkillVersion[]>(`/skill/${id}/versions`);
}

export function listSkillLogs(): Promise<ApiResponse<SkillExecutionLog[]>> {
  return get<SkillExecutionLog[]>('/skill/logs');
}

export function createSkill(data: SkillCreateRequest): Promise<ApiResponse<SkillDef>> {
  return post<SkillDef>('/skill', data);
}

export function updateSkill(id: string, data: Partial<SkillCreateRequest>): Promise<ApiResponse<SkillDef>> {
  return post<SkillDef>(`/skill/${id}`, data);
}

export function deleteSkill(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/skill/${id}`);
}
