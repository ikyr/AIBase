import { get, post, del, type ApiResponse } from './client';

export interface WfCreateRequest {
  name: string;
  description?: string;
  dag?: string;
  timeoutSeconds?: number;
  retryPolicy?: string;
}

export interface WfDefinition {
  id: string;
  name: string;
  description: string;
  version: number;
  dag: string;
  timeoutSeconds: number;
  retryPolicy: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface WfInstance {
  id: string;
  definitionId: string;
  definitionVersion: number;
  status: string;
  input: string;
  output: string;
  context: string;
  startedAt: string;
  completedAt: string;
  errorMsg: string;
  traceId: string;
}

export interface DagView {
  definitionId: string;
  name: string;
  nodes: DagNode[];
  edges: DagEdge[];
  nodeCount: number;
}

export interface DagNode {
  id: string;
  name: string;
  type: string;
  refId: string;
  refName: string;
}

export interface DagEdge {
  from: string;
  to: string;
  label: string;
}

export function listWorkflows(): Promise<ApiResponse<WfDefinition[]>> {
  return get<WfDefinition[]>('/workflow');
}

export function getWorkflowById(id: string): Promise<ApiResponse<WfDefinition>> {
  return get<WfDefinition>(`/workflow/${id}`);
}

export function getWorkflowDag(id: string): Promise<ApiResponse<DagView>> {
  return get<DagView>(`/workflow/${id}/dag`);
}

export function listWorkflowInstances(): Promise<ApiResponse<WfInstance[]>> {
  return get<WfInstance[]>('/workflow/instances');
}

export function createWorkflow(data: WfCreateRequest): Promise<ApiResponse<WfDefinition>> {
  return post<WfDefinition>('/workflow', data);
}

export function updateWorkflow(id: string, data: Partial<WfCreateRequest>): Promise<ApiResponse<WfDefinition>> {
  return post<WfDefinition>(`/workflow/${id}`, data);
}

export function deleteWorkflow(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/workflow/${id}`);
}
