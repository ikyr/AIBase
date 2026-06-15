// src/modules/chat/pages/ChatPage.tsx
import { useEffect, useRef } from 'react';
import SessionList from '../components/SessionList';
import MessageBubble from '../components/MessageBubble';
import ChatInput from '../components/ChatInput';
import { useChatStore } from '../stores/chatStore';
import EmptyState from '../../../shared/components/EmptyState';

export default function ChatPage() {
  const messages = useChatStore((s) => s.messages);
  const activeSessionId = useChatStore((s) => s.activeSessionId);
  const createSession = useChatStore((s) => s.createSession);
  const initAgent = useChatStore((s) => s.initAgent);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => { initAgent(); }, [initAgent]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <SessionList />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#fff' }}>
        {!activeSessionId ? (
          <EmptyState
            icon="💬"
            title="开始一段新对话"
            description="选择或创建一个 Agent 会话，开始 AI 驱动的智能对话"
            actionLabel="新建会话"
            onAction={createSession}
          />
        ) : (
          <>
            <div style={{ flex: 1, overflow: 'auto', padding: '20px' }}>
              {messages.length === 0 ? (
                <div style={{ textAlign: 'center', color: '#999', marginTop: 40, fontSize: 13 }}>
                  发送一条消息开始对话
                </div>
              ) : (
                messages.map((msg) => <MessageBubble key={msg.id} message={msg} />)
              )}
              <div ref={bottomRef} />
            </div>
            <ChatInput />
          </>
        )}
      </div>
    </div>
  );
}
