import { Modal, Form, Input, Select, InputNumber } from 'antd';
import { useSkillStore } from '../stores/skillStore';
import type { SkillCreateRequest } from '../../../shared/api/skill';

interface SkillCreateModalProps {
  open: boolean;
  editData?: SkillCreateRequest & { id?: string };
  onClose: () => void;
}

const levelOptions = [
  { value: 'PROMPT', label: 'Prompt 模板' },
  { value: 'FUNCTION', label: '函数' },
  { value: 'AGENT', label: '子 Agent' },
];

export default function SkillCreateModal({ open, editData, onClose }: SkillCreateModalProps) {
  const [form] = Form.useForm<SkillCreateRequest>();
  const { create, update, loading } = useSkillStore();
  const isEdit = !!editData?.id;
  const level = Form.useWatch('skillLevel', form);

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
      title={isEdit ? '编辑 Skill' : '新建 Skill'}
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
        initialValues={editData ?? { skillLevel: 'PROMPT' }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入 Skill 名称' }]}>
          <Input placeholder="申报书撰写" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="Skill 用途说明" />
        </Form.Item>
        <Form.Item name="skillLevel" label="层级">
          <Select options={levelOptions} />
        </Form.Item>
        <Form.Item name="tags" label="标签 (逗号分隔)">
          <Input placeholder="客服,技术支持" />
        </Form.Item>

        {level === 'PROMPT' && (
          <>
            <Form.Item name="promptTemplate" label="Prompt 模板">
              <Input.TextArea rows={4} placeholder="你是一个{{role}}，请根据以下信息{{input}}..." />
            </Form.Item>
            <Form.Item name="params" label="参数定义 (JSON)">
              <Input.TextArea rows={2} placeholder='{"role":"string","input":"string"}' />
            </Form.Item>
          </>
        )}

        {level === 'FUNCTION' && (
          <>
            <Form.Item name="executionMode" label="执行模式">
              <Select options={[
                { value: 'SYNC', label: '同步' },
                { value: 'ASYNC', label: '异步' },
              ]} />
            </Form.Item>
            <Form.Item name="timeoutMs" label="超时 (ms)">
              <InputNumber min={1000} max={300000} />
            </Form.Item>
            <Form.Item name="inputSchema" label="输入 Schema (JSON)">
              <Input.TextArea rows={3} placeholder='{"type":"object","properties":{...}}' />
            </Form.Item>
            <Form.Item name="outputSchema" label="输出 Schema (JSON)">
              <Input.TextArea rows={3} placeholder='{"type":"object","properties":{...}}' />
            </Form.Item>
          </>
        )}

        {level === 'AGENT' && (
          <Form.Item name="agentRefId" label="关联 Agent ID">
            <Input placeholder="agent_001" />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
}
