import { get, post, type ApiResponse } from './client';

export interface ChatSession {
  id: string;
  title: string;
  lastMessage: string;
  updatedAt: number;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface SendRequest {
  message: string;
}

export interface SendResponse {
  reply: string;
}

export function listChatSessions(): Promise<ApiResponse<ChatSession[]>> {
  return get<ChatSession[]>('chat', '/chat/sessions');
}

export function sendMessage(message: string): Promise<ApiResponse<SendResponse>> {
  return post<SendResponse>('chat', '/chat/send', { message });
}
