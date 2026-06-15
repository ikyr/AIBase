// src/modules/chat/components/ChatInput.tsx
import { useState, useRef, useEffect } from 'react';
import { Button } from 'antd';
import { SendOutlined } from '@ant-design/icons';
import { useChatStore } from '../stores/chatStore';

export default function ChatInput() {
  const [value, setValue] = useState('');
  const loading = useChatStore((s) => s.loading);
  const activeSessionId = useChatStore((s) => s.activeSessionId);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, [activeSessionId]);

  const handleSend = async () => {
    const content = value.trim();
    if (!content || loading) return;
    let sid = useChatStore.getState().activeSessionId;
    if (!sid) {
      await useChatStore.getState().createSession();
      sid = useChatStore.getState().activeSessionId;
    }
    if (sid) {
      useChatStore.getState().sendMessage(content);
    }
    setValue('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        gap: 8,
        padding: '12px 16px',
        borderTop: '1px solid #f0f0f0',
        background: '#fafafa',
        alignItems: 'flex-end',
      }}
    >
      <textarea
        ref={inputRef}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
        rows={1}
        style={{
          flex: 1,
          resize: 'none',
          border: '1px solid #e0e0e0',
          borderRadius: 10,
          padding: '8px 12px',
          fontSize: 13,
          fontFamily: 'inherit',
          lineHeight: 1.5,
          outline: 'none',
          maxHeight: 120,
        }}
      />
      <Button
        type="primary"
        icon={<SendOutlined />}
        onClick={handleSend}
        loading={loading}
        style={{ borderRadius: 10 }}
      >
        发送
      </Button>
    </div>
  );
}
