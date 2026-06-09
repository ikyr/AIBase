import { Modal, Form, Input, Select } from 'antd';
import { useMcpStore } from '../stores/mcpStore';
import type { McpCreateRequest } from '../../../shared/api/mcp';

interface McpCreateModalProps {
  open: boolean;
  editData?: McpCreateRequest & { id?: string };
  onClose: () => void;
}

export default function McpCreateModal({ open, editData, onClose }: McpCreateModalProps) {
  const [form] = Form.useForm<McpCreateRequest>();
  const { create, update, loading } = useMcpStore();
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
      title={isEdit ? '编辑 MCP Server' : '新建 MCP Server'}
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
        initialValues={editData ?? { serverType: 'EXTERNAL', transport: 'SSE' }}
      >
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入 Server 名称' }]}>
          <Input placeholder="外部知识库服务" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="服务说明" />
        </Form.Item>
        <Form.Item name="serverType" label="类型">
          <Select options={[
            { value: 'BUILTIN', label: '内置' },
            { value: 'EXTERNAL', label: '外部' },
          ]} />
        </Form.Item>
        <Form.Item name="transport" label="传输协议">
          <Select options={[
            { value: 'SSE', label: 'SSE' },
            { value: 'STREAMABLE_HTTP', label: 'Streamable HTTP' },
          ]} />
        </Form.Item>
        <Form.Item name="endpoint" label="端点地址">
          <Input placeholder="http://external-service:8081/mcp/sse" />
        </Form.Item>
        <Form.Item name="authConfig" label="认证配置 (JSON)">
          <Input.TextArea rows={2} placeholder='{"type":"API_KEY","key":"sk-xxx"}' />
        </Form.Item>
      </Form>
    </Modal>
  );
}
