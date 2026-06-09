import { Handle, Position, type NodeProps } from '@xyflow/react';

const typeStyle: Record<string, { bg: string; border: string; color: string }> = {
  START:     { bg: '#f6ffed', border: '#52c41a', color: '#389e0d' },
  END:       { bg: '#fff2f0', border: '#ff4d4f', color: '#cf1322' },
  AGENT:     { bg: '#f0f5ff', border: '#1677ff', color: '#0958d9' },
  SKILL:     { bg: '#f9f0ff', border: '#722ed1', color: '#531dab' },
  TOOL:      { bg: '#fff7e6', border: '#fa8c16', color: '#d46b08' },
  KNOWLEDGE: { bg: '#e6fffb', border: '#13c2c2', color: '#08979c' },
  CONDITION: { bg: '#fffbe6', border: '#fadb14', color: '#d4b106' },
  PARALLEL:  { bg: '#f5f5f5', border: '#8c8c8c', color: '#595959' },
  LLM_CALL:  { bg: '#f0f5ff', border: '#2f54eb', color: '#1d39c4' },
  CODE:      { bg: '#262626', border: '#434343', color: '#d9d9d9' },
  WAIT:      { bg: '#fff7e6', border: '#d48806', color: '#ad6800' },
};

export interface WorkflowNodeData {
  label: string;
  nodeType: string;
  refId?: string;
  config?: Record<string, unknown>;
}

export default function WorkflowNode({ data, selected }: NodeProps) {
  const d = data as unknown as WorkflowNodeData;
  const s = typeStyle[d.nodeType] ?? { bg: '#fafafa', border: '#d9d9d9', color: '#666' };

  return (
    <div
      style={{
        padding: '10px 18px',
        borderRadius: d.nodeType === 'CONDITION' ? 12 : 10,
        background: s.bg,
        border: `2px solid ${selected ? '#1677ff' : s.border}`,
        color: s.color,
        fontSize: 13,
        fontWeight: 600,
        textAlign: 'center',
        minWidth: 100,
        boxShadow: selected ? '0 0 0 2px rgba(22,119,255,0.2)' : '0 1px 4px rgba(0,0,0,0.08)',
        cursor: 'pointer',
        transition: 'box-shadow 0.2s, border-color 0.2s',
      }}
    >
      <Handle type="target" position={Position.Top} style={{ background: s.border, width: 8, height: 8 }} />
      <div style={{ fontSize: 10, opacity: 0.7, marginBottom: 2, textTransform: 'uppercase' }}>{d.nodeType}</div>
      <div>{d.label}</div>
      <Handle type="source" position={Position.Bottom} style={{ background: s.border, width: 8, height: 8 }} />
    </div>
  );
}
