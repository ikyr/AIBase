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
  return get<ModelConfig[]>('/model');
}

export function getModelById(id: string): Promise<ApiResponse<ModelConfig>> {
  return get<ModelConfig>(`/model/${id}`);
}

export function listRouteRules(): Promise<ApiResponse<ModelRouteRule[]>> {
  return get<ModelRouteRule[]>('/model/rules');
}

export function listCallLogs(): Promise<ApiResponse<ModelCallLog[]>> {
  return get<ModelCallLog[]>('/model/logs');
}

export function createModel(data: ModelCreateRequest): Promise<ApiResponse<ModelConfig>> {
  return post<ModelConfig>('/model', data);
}

export function updateModel(id: string, data: Partial<ModelCreateRequest>): Promise<ApiResponse<ModelConfig>> {
  return post<ModelConfig>(`/model/${id}`, data);
}

export function deleteModel(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/model/${id}`);
}

// Route rules

export interface RouteRuleCreateRequest {
  name: string;
  modelId: string;
  matchExpression: string;
  priority?: number;
  fallbackModelId?: string;
}

export function createRouteRule(data: RouteRuleCreateRequest): Promise<ApiResponse<ModelRouteRule>> {
  return post<ModelRouteRule>('/model/rules', data);
}

export function deleteRouteRule(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/model/rules/${id}`);
}
