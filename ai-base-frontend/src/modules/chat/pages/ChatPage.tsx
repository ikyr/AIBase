export default function ChatPage() {
  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <div style={{ width: 200, background: '#fafafa', borderRight: '1px solid #f0f0f0', padding: 12 }}>
        <div style={{ fontSize: 11, color: '#999', fontWeight: 600, padding: '8px 10px 4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>会话历史</div>
      </div>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#999', gap: 8 }}>
        <span style={{ fontSize: 32 }}>💬</span>
        <span style={{ fontSize: 14, fontWeight: 500 }}>开始一段新对话</span>
        <span style={{ fontSize: 12 }}>选择一个 Agent 或直接输入问题</span>
      </div>
    </div>
  );
}
