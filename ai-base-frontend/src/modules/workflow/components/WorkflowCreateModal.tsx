import { Modal, Form, Input, InputNumber } from 'antd';
import { useWorkflowStore } from '../stores/workflowStore';
import type { WfCreateRequest } from '../../../shared/api/workflow';

interface WorkflowCreateModalProps {
  open: boolean;
  editData?: WfCreateRequest & { id?: string };
  onClose: () => void;
}

export default function WorkflowCreateModal({ open, editData, onClose }: WorkflowCreateModalProps) {
  const [form] = Form.useForm<WfCreateRequest>();
  const { create, update, loading } = useWorkflowStore();
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
      title={isEdit ? '编辑工作流' : '新建工作流'}
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
        initialValues={editData ?? { timeoutSeconds: 300 }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入工作流名称' }]}>
          <Input placeholder="申报书生成流程" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="工作流用途说明" />
        </Form.Item>
        <Form.Item name="timeoutSeconds" label="超时时间 (秒)">
          <InputNumber min={10} max={3600} />
        </Form.Item>
        <Form.Item name="retryPolicy" label="重试策略 (JSON)">
          <Input.TextArea rows={2} placeholder='{"maxRetries":3,"backoff":"exponential"}' />
        </Form.Item>
      </Form>
    </Modal>
  );
}
