import client from './client';
import type { ApiResponse } from './types';

export type {
  KbConfigInfo,
  KbCreateRequest,
  IngestRequest,
  IngestResult,
  SearchRequest,
  SearchResults,
  KnowledgeStats,
} from './types';
import type {
  KbConfigInfo,
  KbCreateRequest,
  IngestRequest,
  IngestResult,
  SearchRequest,
  SearchResults,
  KnowledgeStats,
} from './types';

export async function createKb(data: KbCreateRequest): Promise<ApiResponse<KbConfigInfo>> {
  return client.post('/knowledge/kb', data) as Promise<ApiResponse<KbConfigInfo>>;
}

export async function listKb(): Promise<ApiResponse<KbConfigInfo[]>> {
  return client.get('/knowledge/kb') as Promise<ApiResponse<KbConfigInfo[]>>;
}

export async function searchKb(data: SearchRequest): Promise<ApiResponse<SearchResults>> {
  return client.post('/knowledge/search', data) as Promise<ApiResponse<SearchResults>>;
}

export async function ingestDocument(data: IngestRequest): Promise<ApiResponse<IngestResult>> {
  return client.post('/knowledge/ingest', data) as Promise<ApiResponse<IngestResult>>;
}

export async function deleteDocument(docId: string): Promise<ApiResponse<null>> {
  return client.delete(`/knowledge/${docId}`) as Promise<ApiResponse<null>>;
}

export async function getKbStats(kbId: string): Promise<ApiResponse<KnowledgeStats>> {
  return client.get(`/knowledge/kb/${kbId}/stats`) as Promise<ApiResponse<KnowledgeStats>>;
}

export async function deleteKb(kbId: string): Promise<ApiResponse<null>> {
  return client.delete(`/knowledge/kb/${kbId}`) as Promise<ApiResponse<null>>;
}
