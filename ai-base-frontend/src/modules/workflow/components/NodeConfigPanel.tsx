import { Drawer, Form, Input, Select, InputNumber } from 'antd';
import type { WorkflowNodeData } from './WorkflowNode';

interface NodeConfigPanelProps {
  open: boolean;
  data: WorkflowNodeData | null;
  nodeId: string;
  onClose: () => void;
  onSave: (nodeId: string, data: WorkflowNodeData) => void;
  onDelete: (nodeId: string) => void;
}

export default function NodeConfigPanel({ open, data, nodeId, onClose, onSave, onDelete }: NodeConfigPanelProps) {
  const [form] = Form.useForm<WorkflowNodeData>();

  const handleSave = () => {
    const values = form.getFieldsValue();
    onSave(nodeId, { ...data!, ...values });
    onClose();
  };

  return (
    <Drawer
      title="节点配置"
      open={open}
      onClose={onClose}
      width={360}
      extra={
        <div style={{ display: 'flex', gap: 8 }}>
          <button
            style={{
              padding: '4px 12px', background: 'transparent', color: '#ff4d4f', border: '1px solid #ff4d4f',
              borderRadius: 6, cursor: 'pointer', fontSize: 13,
            }}
            onClick={() => { onDelete(nodeId); onClose(); }}
          >
            删除
          </button>
          <button
            style={{
              padding: '4px 12px', background: '#1677ff', color: '#fff', border: 'none',
              borderRadius: 6, cursor: 'pointer', fontSize: 13,
            }}
            onClick={handleSave}
          >
            保存
          </button>
        </div>
      }
    >
      <Form form={form} layout="vertical" initialValues={data ?? {}}>
        <Form.Item name="label" label="节点名称" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="refId" label="关联 ID">
          <Input placeholder="Agent ID / Skill ID 等" />
        </Form.Item>

        {data?.nodeType === 'AGENT' && (
          <>
            <Form.Item name={['config', 'agentId']} label="Agent ID">
              <Input placeholder="agent_001" />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'SKILL' && (
          <>
            <Form.Item name={['config', 'skillId']} label="Skill ID">
              <Input placeholder="skill_001" />
            </Form.Item>
            <Form.Item name={['config', 'params']} label="参数 (JSON)">
              <Input.TextArea rows={2} placeholder='{"section":"技术路线"}' />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'TOOL' && (
          <Form.Item name={['config', 'toolName']} label="工具名称">
            <Input placeholder="tool_name" />
          </Form.Item>
        )}
        {data?.nodeType === 'KNOWLEDGE' && (
          <>
            <Form.Item name={['config', 'kbId']} label="知识库 ID">
              <Input placeholder="kb_tech_docs" />
            </Form.Item>
            <Form.Item name={['config', 'topK']} label="Top K">
              <InputNumber min={1} max={100} />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'CONDITION' && (
          <Form.Item name={['config', 'expression']} label="条件表达式">
            <Input placeholder="completeness == 'INCOMPLETE'" />
          </Form.Item>
        )}
        {data?.nodeType === 'LLM_CALL' && (
          <>
            <Form.Item name={['config', 'model']} label="模型">
              <Select options={[
                { value: 'qwen-plus', label: 'qwen-plus' },
                { value: 'gpt-4o', label: 'gpt-4o' },
              ]} />
            </Form.Item>
            <Form.Item name={['config', 'prompt']} label="Prompt">
              <Input.TextArea rows={4} placeholder="LLM 提示词..." />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'CODE' && (
          <>
            <Form.Item name={['config', 'language']} label="语言">
              <Select options={[{ value: 'javascript', label: 'JavaScript' }]} />
            </Form.Item>
            <Form.Item name={['config', 'code']} label="代码">
              <Input.TextArea rows={6} placeholder="// 脚本代码..." style={{ fontFamily: 'monospace' }} />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'WAIT' && (
          <>
            <Form.Item name={['config', 'title']} label="审批标题">
              <Input placeholder="申报书审批" />
            </Form.Item>
            <Form.Item name={['config', 'assigneeRole']} label="审批角色">
              <Input placeholder="PM" />
            </Form.Item>
          </>
        )}
        {data?.nodeType === 'PARALLEL' && (
          <Form.Item name={['config', 'children']} label="子节点 ID (逗号分隔)">
            <Input placeholder="node_001,node_002" />
          </Form.Item>
        )}
      </Form>
    </Drawer>
  );
}
