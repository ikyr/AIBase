// src/modules/chat/components/MessageBubble.tsx
import type { Message } from '../stores/chatStore';

interface MessageBubbleProps {
  message: Message;
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'user';
  return (
    <div style={{ display: 'flex', justifyContent: isUser ? 'flex-end' : 'flex-start', marginBottom: 6 }}>
      <div
        style={{
          maxWidth: '80%',
          padding: '10px 14px',
          borderRadius: 12,
          borderBottomLeftRadius: isUser ? 12 : 4,
          borderBottomRightRadius: isUser ? 4 : 12,
          background: isUser ? '#f0f5ff' : '#f5f5f5',
          color: isUser ? '#1a1a1a' : '#333',
          fontSize: 13,
          lineHeight: 1.5,
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
        }}
      >
        {message.content}
      </div>
    </div>
  );
}
