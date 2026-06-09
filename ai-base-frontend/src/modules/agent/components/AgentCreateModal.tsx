import { Modal, Form, Input, Select } from 'antd';
import { useAgentStore } from '../stores/agentStore';
import type { AgentCreateRequest } from '../../../shared/api/agent';

interface AgentCreateModalProps {
  open: boolean;
  editData?: AgentCreateRequest & { id?: string };
  onClose: () => void;
}

const modeOptions = [
  { value: 'REACT', label: 'ReAct 推理' },
  { value: 'GRAPH', label: 'Graph 编排' },
  { value: 'NEGOTIATION', label: '协商' },
];

export default function AgentCreateModal({ open, editData, onClose }: AgentCreateModalProps) {
  const [form] = Form.useForm<AgentCreateRequest>();
  const { create, update, loading } = useAgentStore();
  const isEdit = !!editData?.id;

  const handleOk = async () => {
    const values = await form.validateFields();
    if (isEdit) {
      await update(editData!.id!, values);
    } else {
      await create(values);
    }
    onClose();
    form.resetFields();
  };

  return (
    <Modal
      title={isEdit ? '编辑 Agent' : '新建 Agent'}
      open={open}
      onOk={handleOk}
      onCancel={() => { onClose(); form.resetFields(); }}
      confirmLoading={loading}
      okText={isEdit ? '保存' : '创建'}
      cancelText="取消"
      width={640}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={editData ?? { coordinationMode: 'REACT', model: 'qwen-plus' }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入 Agent 名称' }]}>
          <Input placeholder="技术支持 Agent" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="Agent 用途说明" />
        </Form.Item>
        <Form.Item name="systemPrompt" label="System Prompt">
          <Input.TextArea rows={4} placeholder="系统提示词..." />
        </Form.Item>
        <Form.Item name="model" label="模型">
          <Select options={[
            { value: 'qwen-plus', label: 'qwen-plus' },
            { value: 'qwen-max', label: 'qwen-max' },
            { value: 'qwen-turbo', label: 'qwen-turbo' },
            { value: 'gpt-4o', label: 'gpt-4o' },
            { value: 'deepseek-chat', label: 'deepseek-chat' },
          ]} />
        </Form.Item>
        <Form.Item name="coordinationMode" label="协调模式">
          <Select options={modeOptions} />
        </Form.Item>
        <Form.Item name="tools" label="工具 (逗号分隔)">
          <Input placeholder="kb_search,ticket_query" />
        </Form.Item>
        <Form.Item name="skillIds" label="Skill ID (逗号分隔)">
          <Input placeholder="skill_001,skill_002" />
        </Form.Item>
        <Form.Item name="kbIds" label="知识库 ID (逗号分隔)">
          <Input placeholder="kb_tech_docs,kb_faq" />
        </Form.Item>
        <Form.Item name="constraints" label="约束 (JSON)">
          <Input.TextArea rows={2} placeholder='{"maxSteps":20,"temperature":0.3}' />
        </Form.Item>
      </Form>
    </Modal>
  );
}
