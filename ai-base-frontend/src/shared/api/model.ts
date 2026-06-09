import { get, post, del, type ApiResponse } from './client';

export interface ModelCreateRequest {
  name: string;
  provider?: string;
  endpoint?: string;
  apiKeyRef?: string;
  maxTokens?: number;
  capabilities?: string;
  priority?: number;
}

export interface ModelConfig {
  id: string;
  name: string;
  provider: string;
  endpoint: string;
  apiKeyRef: string;
  maxTokens: number;
  capabilities: string;
  priority: number;
  status: string;
  createdAt: string;
}

export interface ModelRouteRule {
  id: string;
  name: string;
  modelId: string;
  matchExpression: string;
  priority: number;
  fallbackModelId: string;
  status: string;
}

export interface ModelCallLog {
  id: string;
  modelName: string;
  callerRef: string;
  inputTokens: number;
  outputTokens: number;
  durationMs: number;
  cost: string;
  callStatus: string;
  errorMsg: string;
  createdAt: string;
}

export function listModels(): Promise<ApiResponse<ModelConfig[]>> {
  return get<ModelConfig[]>('model', '/model');
}

export function getModelById(id: string): Promise<ApiResponse<ModelConfig>> {
  return get<ModelConfig>('model', `/model/${id}`);
}

export function listRouteRules(): Promise<ApiResponse<ModelRouteRule[]>> {
  return get<ModelRouteRule[]>('model', '/model/rules');
}

export function listCallLogs(): Promise<ApiResponse<ModelCallLog[]>> {
  return get<ModelCallLog[]>('model', '/model/logs');
}

export function createModel(data: ModelCreateRequest): Promise<ApiResponse<ModelConfig>> {
  return post<ModelConfig>('model', '/model', data);
}

export function updateModel(id: string, data: Partial<ModelCreateRequest>): Promise<ApiResponse<ModelConfig>> {
  return post<ModelConfig>('model', `/model/${id}`, data);
}

export function deleteModel(id: string): Promise<ApiResponse<null>> {
  return del<null>('model', `/model/${id}`);
}
