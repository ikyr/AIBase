// src/modules/chat/stores/chatStore.ts
import { create } from 'zustand';

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
    }));
  },

  switchSession: (id: string) => {
    set({ activeSessionId: id, messages: [] });
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

    set({ messages: [...messages, userMsg], loading: true });

    // Simulate AI response (real API call when backend ready)
    await new Promise((r) => setTimeout(r, 800));

    const aiMsg: Message = {
      id: `msg-${nextId++}`,
      role: 'assistant',
      content: `收到你的消息：「${content}」。当前为模拟回复，后端 Agent 服务接入后将返回真实 AI 响应。`,
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
  },
}));
