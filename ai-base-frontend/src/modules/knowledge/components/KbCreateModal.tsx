// src/modules/knowledge/components/KbCreateModal.tsx
import { Modal, Form, Input, Select, InputNumber } from 'antd';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import type { KbCreateRequest } from '../../../shared/api/types';

interface KbCreateModalProps {
  open: boolean;
  onClose: () => void;
}

export default function KbCreateModal({ open, onClose }: KbCreateModalProps) {
  const [form] = Form.useForm<KbCreateRequest>();
  const create = useKnowledgeStore((s) => s.create);
  const loading = useKnowledgeStore((s) => s.loading);

  const handleOk = async () => {
    const values = await form.validateFields();
    await create(values);
    onClose();
    form.resetFields();
  };

  return (
    <Modal
      title="创建知识库"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={loading}
      okText="创建"
      cancelText="取消"
    >
      <Form form={form} layout="vertical" initialValues={{ kbType: 'PUBLIC', embeddingModel: 'text-embedding-v3', chunkSize: 800, chunkOverlap: 100 }}>
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入知识库名称' }]}>
          <Input placeholder="我的知识库" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="知识库用途说明" />
        </Form.Item>
        <Form.Item name="kbType" label="类型">
          <Select options={[
            { value: 'PUBLIC', label: '公共' },
            { value: 'PERSONAL', label: '个人' },
            { value: 'DEPARTMENT', label: '部门' },
          ]} />
        </Form.Item>
        <Form.Item name="embeddingModel" label="Embedding 模型">
          <Select options={[{ value: 'text-embedding-v3', label: 'text-embedding-v3' }]} />
        </Form.Item>
        <Form.Item name="chunkSize" label="分块大小">
          <InputNumber min={100} max={4000} />
        </Form.Item>
        <Form.Item name="chunkOverlap" label="重叠大小">
          <InputNumber min={0} max={500} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
