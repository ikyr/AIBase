import { useEffect, useState } from 'react';
import { Card, Spin, Table, Tag, Modal, Form, Input, Select, InputNumber } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useModelStore } from '../stores/modelStore';
import type { ModelRouteRule, RouteRuleCreateRequest } from '../../../shared/api/model';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const columns: ColumnsType<ModelRouteRule> = [
  { title: '规则名称', dataIndex: 'name', key: 'name', render: (t: string) => <span style={{ fontWeight: 500 }}>{t}</span> },
  { title: '目标模型', dataIndex: 'modelId', key: 'modelId', width: 150, render: (t: string) => <Tag style={{ borderRadius: 6, fontSize: 11 }}>{t}</Tag> },
  { title: '匹配条件', dataIndex: 'matchExpression', key: 'matchExpression', width: 200, render: (c: string) => <code style={{ fontSize: 11 }}>{c}</code> },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 70, align: 'center' },
  { title: '降级模型', dataIndex: 'fallbackModelId', key: 'fallbackModelId', width: 150, render: (t: string) => <Tag style={{ borderRadius: 6, fontSize: 11, color: '#888' }}>{t || '-'}</Tag> },
  { title: '状态', dataIndex: 'status', key: 'status', width: 70, render: (s: string) => <StatusTag status={s} /> },
];

export default function ModelRouteRulePage() {
  const { rules, models, loading, fetchRules, fetchList, createRule } = useModelStore();
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<RouteRuleCreateRequest>();

  useEffect(() => { fetchRules(); fetchList(); }, [fetchRules, fetchList]);

  const handleCreate = async () => {
    const values = await form.validateFields();
    await createRule(values);
    setModalOpen(false);
    form.resetFields();
  };

  return (
    <div>
      <PageHeader title="路由规则" actionLabel="新建规则" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={rules.length} label="规则总数" />
          <StatCard value={rules.filter((r) => r.status === 'ACTIVE').length} label="已启用" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : rules.length === 0 ? (
          <EmptyState icon="🔀" title="暂无路由规则" description="配置路由规则来分流模型调用" actionLabel="新建规则" onAction={() => setModalOpen(true)} />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={rules}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 条规则` }}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>

      <Modal
        title="新建路由规则"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        confirmLoading={loading}
        okText="创建"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" initialValues={{ priority: 0 }}>
          <Form.Item name="name" label="规则名称" rules={[{ required: true, message: '请输入规则名称' }]}>
            <Input placeholder="例如: 高优先级路由" />
          </Form.Item>
          <Form.Item name="modelId" label="目标模型" rules={[{ required: true, message: '请选择目标模型' }]}>
            <Select
              showSearch
              placeholder="选择模型"
              options={models.filter((m) => m.status === 'ACTIVE').map((m) => ({ value: m.id, label: m.name }))}
            />
          </Form.Item>
          <Form.Item name="matchExpression" label="匹配条件" rules={[{ required: true, message: '请输入匹配条件' }]}>
            <Input placeholder="例如: capability == chat" />
          </Form.Item>
          <Form.Item name="priority" label="优先级">
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="fallbackModelId" label="降级模型 (可选)">
            <Select
              allowClear
              showSearch
              placeholder="选择降级模型"
              options={models.filter((m) => m.status === 'ACTIVE').map((m) => ({ value: m.id, label: m.name }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
