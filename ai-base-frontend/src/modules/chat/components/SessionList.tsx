// src/modules/chat/components/SessionList.tsx
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useChatStore } from '../stores/chatStore';

export default function SessionList() {
  const sessions = useChatStore((s) => s.sessions);
  const activeId = useChatStore((s) => s.activeSessionId);
  const createSession = useChatStore((s) => s.createSession);
  const switchSession = useChatStore((s) => s.switchSession);

  return (
    <div style={{ width: 200, background: '#fafafa', borderRight: '1px solid #f0f0f0', display: 'flex', flexDirection: 'column', flexShrink: 0 }}>
      <div style={{ padding: '12px 8px', borderBottom: '1px solid #f0f0f0' }}>
        <Button block icon={<PlusOutlined />} onClick={createSession} size="small">
          新建会话
        </Button>
      </div>
      <div style={{ flex: 1, overflow: 'auto', padding: '8px', display: 'flex', flexDirection: 'column', gap: 2 }}>
        {sessions.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#999', fontSize: 12, padding: 20 }}>
            暂无会话，点击上方按钮开始
          </div>
        ) : (
          sessions.map((s) => (
            <button
              key={s.id}
              onClick={() => switchSession(s.id)}
              style={{
                width: '100%',
                textAlign: 'left',
                padding: '10px',
                borderRadius: 8,
                border: 'none',
                background: s.id === activeId ? '#fff' : 'transparent',
                boxShadow: s.id === activeId ? '0 1px 3px rgba(0,0,0,0.04)' : 'none',
                cursor: 'pointer',
                fontFamily: 'inherit',
                transition: 'all 0.15s',
              }}
            >
              <div style={{ fontSize: 13, fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {s.title}
              </div>
              <div style={{ fontSize: 11, color: '#999', marginTop: 2 }}>
                {new Date(s.updatedAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
              </div>
            </button>
          ))
        )}
      </div>
    </div>
  );
}
