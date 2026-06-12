import type { DragEvent } from 'react';

const nodeTypes = [
  { type: 'START', label: '开始', color: '#52c41a' },
  { type: 'END', label: '结束', color: '#ff4d4f' },
  { type: 'AGENT', label: 'Agent', color: '#1677ff' },
  { type: 'SKILL', label: 'Skill', color: '#722ed1' },
  { type: 'TOOL', label: 'Tool', color: '#fa8c16' },
  { type: 'KNOWLEDGE', label: '知识检索', color: '#13c2c2' },
  { type: 'CONDITION', label: '条件', color: '#fadb14' },
  { type: 'PARALLEL', label: '并行', color: '#8c8c8c' },
  { type: 'LLM_CALL', label: 'LLM 调用', color: '#2f54eb' },
  { type: 'CODE', label: '脚本', color: '#434343' },
  { type: 'WAIT', label: '等待', color: '#d48806' },
  { type: 'QUESTION_CLASSIFIER', label: '问题分类', color: '#eb2f96' },
  { type: 'VARIABLE_ASSIGNER', label: '变量赋值', color: '#faad14' },
  { type: 'HTTP_REQUEST', label: 'HTTP 请求', color: '#fa541c' },
];

export default function NodePalette() {
  const onDragStart = (e: DragEvent, nodeType: string) => {
    e.dataTransfer.setData('application/reactflow-type', nodeType);
    e.dataTransfer.effectAllowed = 'move';
  };

  return (
    <div style={{ width: 180, background: '#fff', borderRight: '1px solid #f0f0f0', padding: 16, overflowY: 'auto' }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: '#999', marginBottom: 12, textTransform: 'uppercase' }}>
        节点类型
      </div>
      {nodeTypes.map((nt) => (
        <div
          key={nt.type}
          draggable
          onDragStart={(e) => onDragStart(e, nt.type)}
          style={{
            padding: '8px 12px',
            marginBottom: 8,
            borderRadius: 8,
            border: `1px solid ${nt.color}40`,
            background: `${nt.color}10`,
            color: nt.color,
            fontSize: 13,
            fontWeight: 500,
            cursor: 'grab',
            textAlign: 'center',
            userSelect: 'none',
            transition: 'transform 0.1s, box-shadow 0.1s',
          }}
          onMouseDown={(e) => {
            (e.currentTarget as HTMLDivElement).style.transform = 'scale(0.97)';
          }}
          onMouseUp={(e) => {
            (e.currentTarget as HTMLDivElement).style.transform = 'scale(1)';
          }}
        >
          {nt.label}
        </div>
      ))}
    </div>
  );
}
