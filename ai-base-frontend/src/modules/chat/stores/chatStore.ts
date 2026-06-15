import { create } from 'zustand';
import client from '@/shared/api/client';
import { listAgents } from '@/shared/api/agent';
import type { ApiResponse } from '@/shared/api/types';

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
}

export interface Session {
  id: string;
  title: string;
  lastMessage: string;
  updatedAt: number;
}

interface ChatState {
  sessions: Session[];
  activeSessionId: string | null;
  messages: Message[];
  loading: boolean;
  error: string | null;
  agentId: string | null;
  initAgent: () => Promise<void>;
  createSession: () => Promise<void>;
  switchSession: (id: string) => Promise<void>;
  sendMessage: (content: string) => Promise<void>;
}

export const useChatStore = create<ChatState>((set, get) => ({
  sessions: [],
  activeSessionId: null,
  messages: [],
  loading: false,
  error: null,
  agentId: null,

  initAgent: async () => {
    try {
      const res = await listAgents();
      if (res.success && res.data && res.data.length > 0) {
        const activeAgent = res.data.find((a) => a.status === 'ACTIVE') ?? res.data[0];
        set({ agentId: activeAgent.id });
      }
    } catch {
      // Agent list unavailable — sessions will be local-only
    }
  },

  createSession: async () => {
    const { agentId } = get();
    set({ loading: true, error: null });

    if (agentId) {
      try {
        const res = await client.post(`/agent/${agentId}/sessions`) as unknown as ApiResponse<{ id: string; title: string }>;
        if (res.success && res.data) {
          const session: Session = {
            id: res.data.id,
            title: res.data.title || '新对话',
            lastMessage: '',
            updatedAt: Date.now(),
          };
          set((s) => ({
            sessions: [session, ...s.sessions],
            activeSessionId: session.id,
            messages: [],
            loading: false,
          }));
          return;
        }
      } catch {
        // Fallback to local session if backend fails
      }
    }

    // Fallback: local-only session
    const id = `local-${Date.now()}`;
    const session: Session = { id, title: '新对话', lastMessage: '', updatedAt: Date.now() };
    set((s) => ({
      sessions: [session, ...s.sessions],
      activeSessionId: id,
      messages: [],
      loading: false,
    }));
  },

  switchSession: async (id: string) => {
    set({ activeSessionId: id, messages: [], loading: true, error: null });

    // If it's a backend session (not local- prefix), load messages
    if (!id.startsWith('local-')) {
      try {
        const res = await client.get(`/agent/sessions/${id}/messages`) as unknown as ApiResponse<Array<{ id: string; role: string; content: string }>>;
        if (res.success && res.data) {
          const messages: Message[] = res.data.map((m) => ({
            id: m.id,
            role: m.role === 'assistant' ? 'assistant' : 'user',
            content: m.content,
            timestamp: Date.now(),
          }));
          set({ messages, loading: false });
          return;
        }
      } catch {
        // Messages unavailable — start with empty
      }
    }
    set({ loading: false });
  },

  sendMessage: async (content: string) => {
    const { activeSessionId, messages, agentId } = get();
    if (!activeSessionId) return;

    const userMsg: Message = {
      id: `msg-${Date.now()}`,
      role: 'user',
      content,
      timestamp: Date.now(),
    };

    const aiMsgId = `msg-${Date.now() + 1}`;
    const aiMsg: Message = {
      id: aiMsgId,
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
    };

    set({ messages: [...messages, userMsg, aiMsg], loading: true, error: null });

    const body: Record<string, unknown> = { message: content };
    if (activeSessionId) body.sessionId = activeSessionId;
    if (agentId) body.agentId = agentId;

    // Try SSE streaming first
    try {
      const response = await fetch('/api/v1/agent/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Api-Key': localStorage.getItem('apiKey') || 'aibase-dev-key-2024',
        },
        body: JSON.stringify(body),
      });

      if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
        const reader = response.body?.getReader();
        const decoder = new TextDecoder();
        let accumulated = '';
        let buffer = '';

        if (reader) {
          while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';

            for (const line of lines) {
              if (line.startsWith('data:')) {
                const raw = line.slice(5).trim();
                if (raw.startsWith('{')) {
                  try {
                    const parsed = JSON.parse(raw);
                    if (parsed.hasOwnProperty('messageId') || parsed.hasOwnProperty('done')) {
                      continue; // skip metadata events
                    }
                    accumulated += parsed;
                  } catch {
                    accumulated += raw;
                  }
                } else {
                  accumulated += raw;
                }
                // Update AI message in real-time
                set((s) => ({
                  messages: s.messages.map((m) =>
                    m.id === aiMsgId ? { ...m, content: accumulated } : m
                  ),
                }));
              }
            }
          }
        }

        // Final update
        set((s) => ({
          loading: false,
          messages: s.messages.map((m) =>
            m.id === aiMsgId ? { ...m, content: accumulated || '未收到回复' } : m
          ),
          sessions: s.sessions.map((sess) =>
            sess.id === activeSessionId
              ? { ...sess, lastMessage: content, updatedAt: Date.now(), title: content.slice(0, 20) }
              : sess
          ),
        }));
        return;
      }
    } catch {
      // SSE not available — fall through to non-streaming
    }

    // Fallback: non-streaming request
    try {
      const response = await client.post('/agent/chat', body) as unknown as ApiResponse<{ content?: string; reply?: string; toolCalls?: unknown[] }>;

      if (!response.success) {
        throw new Error(response.error || 'Agent returned an error');
      }

      const replyContent = response.data?.content || response.data?.reply || '未收到回复';

      set((s) => ({
        messages: s.messages.map((m) =>
          m.id === aiMsgId ? { ...m, content: replyContent } : m
        ),
        loading: false,
        sessions: s.sessions.map((sess) =>
          sess.id === activeSessionId
            ? { ...sess, lastMessage: content, updatedAt: Date.now(), title: content.slice(0, 20) }
            : sess
        ),
      }));
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '发送消息失败';
      set({ loading: false, error: message });
    }
  },
}));
