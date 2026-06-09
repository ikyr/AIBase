// src/shared/api/types.ts

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: string | null;
}

export interface PageRequest {
  page: number;
  size: number;
}

export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

// --- Knowledge Base ---

export interface KbConfigInfo {
  id: string;
  name: string;
  description: string;
  kbType: 'PUBLIC' | 'PERSONAL' | 'DEPARTMENT';
  ownerId: string | null;
  ownerDeptId: string | null;
  embeddingModel: string;
  chunkSize: number;
  chunkOverlap: number;
  documentCount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface KbCreateRequest {
  name: string;
  description?: string;
  kbType?: string;
  embeddingModel?: string;
  chunkSize?: number;
  chunkOverlap?: number;
}

export interface IngestRequest {
  kbId: string;
  title: string;
  content: string;
  sourceType?: 'UPLOAD' | 'CONNECTOR' | 'SEARCH' | 'API';
  sourceRef?: string;
  fileType?: string;
}

export interface IngestResult {
  docId: string;
  status: string;
  chunkCount: number;
}

export interface SearchRequest {
  kbId: string;
  query: string;
  topK?: number;
  strategy?: 'VECTOR' | 'KEYWORD' | 'HYBRID';
}

export interface SearchResult {
  query: string;
  tookMs: number;
  hits: SearchHit[];
}

export interface SearchHit {
  docId: string;
  chunkIndex: number;
  content: string;
  score: number;
  metadata?: Record<string, string>;
}

export interface KnowledgeStats {
  kbId: string;
  documentCount?: number;
  chunkCount?: number;
  totalTokens?: number;
}

// --- Agent ---

export interface AgentChatRequest {
  agentId?: string;
  sessionId?: string;
  message: string;
}

export interface AgentChatResponse {
  sessionId: string;
  message: string;
  toolCalls?: unknown[];
  done: boolean;
}

export interface SessionDetail {
  id: string;
  agentId: string;
  title: string;
  status: string;
  messages: unknown[];
}

// --- Workflow ---

export interface WorkflowExecuteRequest {
  definitionId: string;
  input: Record<string, unknown>;
}

export interface WorkflowResult {
  instanceId: string;
  status: string;
  output: Record<string, unknown> | null;
  errorMsg: string | null;
}
