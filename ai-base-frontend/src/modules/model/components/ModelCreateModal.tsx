import { Modal, Form, Input, Select, InputNumber } from 'antd';
import { useModelStore } from '../stores/modelStore';
import type { ModelCreateRequest } from '../../../shared/api/model';

interface ModelCreateModalProps {
  open: boolean;
  editData?: ModelCreateRequest & { id?: string };
  onClose: () => void;
}

export default function ModelCreateModal({ open, editData, onClose }: ModelCreateModalProps) {
  const [form] = Form.useForm<ModelCreateRequest>();
  const { create, update, loading } = useModelStore();
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
      title={isEdit ? '编辑模型' : '新建模型'}
      open={open}
      onOk={handleOk}
      onCancel={() => { onClose(); form.resetFields(); }}
      confirmLoading={loading}
      okText={isEdit ? '保存' : '创建'}
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={editData ?? { provider: 'OPENAI', maxTokens: 4096, priority: 0 }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入模型名称' }]}>
          <Input placeholder="gpt-4o" />
        </Form.Item>
        <Form.Item name="provider" label="提供商">
          <Select options={[
            { value: 'OPENAI', label: 'OpenAI / 兼容 API' },
            { value: 'DASHSCOPE', label: '阿里云 DashScope' },
            { value: 'LOCAL', label: '本地部署' },
          ]} />
        </Form.Item>
        <Form.Item name="endpoint" label="API 端点">
          <Input placeholder="https://api.openai.com (留空使用默认)" />
        </Form.Item>
        <Form.Item name="apiKeyRef" label="API Key 环境变量名">
          <Input placeholder="OPENAI_API_KEY" />
        </Form.Item>
        <Form.Item name="maxTokens" label="最大 Token">
          <InputNumber min={256} max={128000} />
        </Form.Item>
        <Form.Item name="capabilities" label="能力 (逗号分隔)">
          <Input placeholder="chat,embed" />
        </Form.Item>
        <Form.Item name="priority" label="优先级">
          <InputNumber min={0} max={100} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
