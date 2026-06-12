import { get, post, del, type ApiResponse } from './client';

export interface EvalDataset {
  id: string;
  name: string;
  description: string;
  evalType: string;
  itemCount: number;
  status: string;
  createdAt: string;
}

export interface EvalTask {
  id: string;
  datasetId: string;
  targetId: string;
  targetType: string;
  status: string;
  metrics: string;
  totalItems: number;
  passedItems: number;
  startedAt: string;
  completedAt: string;
  traceId: string;
}

export interface EvalResult {
  id: string;
  taskId: string;
  itemId: string;
  actualOutput: string;
  metrics: string;
  passed: boolean;
  errorMsg: string | null;
  durationMs: number;
  createdAt: string;
}

export interface CreateDatasetRequest {
  name: string;
  description?: string;
  evalType: string;
}

export interface CreateTaskRequest {
  datasetId: string;
  targetId: string;
  targetType: string;
}

export interface ExecuteResult {
  taskId: string;
  status: string;
  metrics: string;
}

// Datasets
export function listEvalDatasets(): Promise<ApiResponse<EvalDataset[]>> {
  return get<EvalDataset[]>('/eval/datasets');
}

export function createEvalDataset(data: CreateDatasetRequest): Promise<ApiResponse<EvalDataset>> {
  return post<EvalDataset>('/eval/datasets', data);
}

export function deleteEvalDataset(id: string): Promise<ApiResponse<null>> {
  return del<null>(`/eval/datasets/${id}`);
}

// Tasks
export function listEvalTasks(): Promise<ApiResponse<EvalTask[]>> {
  return get<EvalTask[]>('/eval/tasks');
}

export function createEvalTask(data: CreateTaskRequest): Promise<ApiResponse<EvalTask>> {
  return post<EvalTask>('/eval/tasks', data);
}

export function executeEvalTask(taskId: string): Promise<ApiResponse<ExecuteResult>> {
  return post<ExecuteResult>(`/eval/tasks/${taskId}/execute`);
}

// Results
export function getEvalResults(taskId: string): Promise<ApiResponse<EvalResult[]>> {
  return get<EvalResult[]>(`/eval/results/${taskId}`);
}
