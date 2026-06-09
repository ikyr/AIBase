import { create } from 'zustand';
import { post } from '@/shared/api/client';

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
  createSession: () => void;
  switchSession: (id: string) => void;
  sendMessage: (content: string) => Promise<void>;
}

let nextId = 1;

export const useChatStore = create<ChatState>((set, get) => ({
  sessions: [],
  activeSessionId: null,
  messages: [],
  loading: false,
  error: null,

  createSession: () => {
    const id = `session-${nextId++}`;
    const session: Session = {
      id,
      title: '新对话',
      lastMessage: '',
      updatedAt: Date.now(),
    };
    set((s) => ({
      sessions: [session, ...s.sessions],
      activeSessionId: id,
      messages: [],
      error: null,
    }));
  },

  switchSession: (id: string) => {
    set({ activeSessionId: id, messages: [], error: null });
  },

  sendMessage: async (content: string) => {
    const { activeSessionId, messages } = get();
    if (!activeSessionId) return;

    const userMsg: Message = {
      id: `msg-${nextId++}`,
      role: 'user',
      content,
      timestamp: Date.now(),
    };

    set({ messages: [...messages, userMsg], loading: true, error: null });

    try {
      const response = await post<{ reply: string }>('/agent/chat', {
        sessionId: activeSessionId,
        message: content,
      });

      if (!response.success) {
        throw new Error(response.error || 'Agent returned an error');
      }

      const aiMsg: Message = {
        id: `msg-${nextId++}`,
        role: 'assistant',
        content: response.data?.reply ?? '未收到回复',
        timestamp: Date.now(),
      };

      set((s) => ({
        messages: [...s.messages, aiMsg],
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
