import { get, type ApiResponse } from './client';

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
  errorMsg: string;
  durationMs: number;
  createdAt: string;
}

export function listEvalDatasets(): Promise<ApiResponse<EvalDataset[]>> {
  return get<EvalDataset[]>('eval', '/eval/datasets');
}

export function listEvalTasks(): Promise<ApiResponse<EvalTask[]>> {
  return get<EvalTask[]>('eval', '/eval/tasks');
}

export function getEvalResults(taskId: string): Promise<ApiResponse<EvalResult[]>> {
  return get<EvalResult[]>('eval', `/eval/results/${taskId}`);
}
