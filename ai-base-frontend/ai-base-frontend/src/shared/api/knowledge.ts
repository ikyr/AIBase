import { get, post, type ApiResponse } from './client';

export interface KbConfigInfo {
  id: string;
  name: string;
  description: string;
  connectorType: string;
  documentCount: number;
  docCount: number;
  chunkCount: number;
  kbType?: string;
  chunkSize?: number;
  chunkOverlap?: number;
  status: string;
  createdAt: string;
}

export interface KbDocument {
  id: string;
  kbId: string;
  fileName: string;
  fileSize: string;
  fileType: string;
  chunkCount: number;
  uploadStatus: string;
  uploadedAt: string;
}

export function listKb(): Promise<ApiResponse<KbConfigInfo[]>> {
  return get<KbConfigInfo[]>('knowledge', '/knowledge');
}

export function getKb(id: string): Promise<ApiResponse<KbConfigInfo>> {
  return get<KbConfigInfo>('knowledge', `/knowledge/${id}`);
}

export function getKbDocuments(kbId: string): Promise<ApiResponse<KbDocument[]>> {
  return get<KbDocument[]>('knowledge', `/knowledge/${kbId}/documents`);
}

export function createKb(data: { name: string; description?: string }): Promise<ApiResponse<KbConfigInfo>> {
  return post<KbConfigInfo>('knowledge', '/knowledge', data);
}
