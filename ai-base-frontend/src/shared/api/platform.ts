import { get, post, type ApiResponse } from './client';

export interface PromptVersion {
  id: string;
  refType: string;
  refId: string;
  version: number;
  content: string;
  changelog: string;
  isCurrent: boolean;
  status: string;
  createdAt: string;
  createdBy: string;
}

export interface ApprovalRecord {
  id: string;
  type: string;
  refType: string;
  refId: string;
  refName: string;
  requester: string;
  approvers: string;
  status: string;
  reason: string;
  createdAt: string;
}

export function listPrompts(): Promise<ApiResponse<PromptVersion[]>> {
  return get<PromptVersion[]>('/platform/prompts');
}

export function listApprovals(): Promise<ApiResponse<ApprovalRecord[]>> {
  return get<ApprovalRecord[]>('/platform/approvals');
}

export function approveApproval(id: string): Promise<ApiResponse<ApprovalRecord>> {
  return post<ApprovalRecord>(`/platform/approvals/${id}/approve`);
}

export function rejectApproval(id: string): Promise<ApiResponse<ApprovalRecord>> {
  return post<ApprovalRecord>(`/platform/approvals/${id}/reject`);
}
