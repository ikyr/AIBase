import { useEffect, useState } from 'react';
import { Card, Spin, Table, Tag, Button, Modal, Form, Input, Select, message } from 'antd';
import { PlayCircleOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { useEvalStore } from '../stores/evalStore';
import type { EvalTask, CreateTaskRequest } from '../../../shared/api/eval';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const statusTag: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'default', label: '等待中' },
  RUNNING: { color: 'processing', label: '运行中' },
  COMPLETED: { color: 'success', label: '已完成' },
  FAILED: { color: 'error', label: '失败' },
};

export default function EvalTaskListPage() {
  const { tasks, datasets, loading, fetchTasks, fetchList, createTask, executeTask } = useEvalStore();
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm<CreateTaskRequest>();
  const navigate = useNavigate();

  useEffect(() => { fetchTasks(); fetchList(); }, [fetchTasks, fetchList]);

  const handleCreate = async () => {
    const values = await form.validateFields();
    await createTask(values);
    setCreateOpen(false);
    form.resetFields();
    message.success('任务创建成功');
  };

  const handleExecute = async (taskId: string) => {
    message.loading({ content: '正在执行评估...', key: 'exec', duration: 0 });
    await executeTask(taskId);
    message.success({ content: '评估执行完成', key: 'exec' });
    await fetchTasks();
  };

  const parseMetrics = (m: string): Record<string, string> => {
    try { return JSON.parse(m) as Record<string, string>; } catch { return {}; }
  };

  const columns: ColumnsType<EvalTask> = [
    { title: '任务 ID', dataIndex: 'id', key: 'id', width: 100, render: (t: string) => <span style={{ fontWeight: 500 }}>{t.slice(0, 8)}...</span> },
    { title: '数据集', dataIndex: 'datasetId', key: 'datasetId', width: 110, ellipsis: true },
    { title: '目标', dataIndex: 'targetType', key: 'targetType', width: 90,
      render: (t: string) => <Tag style={{ borderRadius: 6, fontSize: 11 }}>{t}</Tag>,
    },
    { title: '通过/总数', key: 'progress', width: 90, align: 'center',
      render: (_: unknown, r: EvalTask) => <span>{r.passedItems ?? '-'} / {r.totalItems ?? '-'}</span>,
    },
    { title: '成功率', key: 'rate', width: 80, align: 'center',
      render: (_: unknown, r: EvalTask) => {
        const m = parseMetrics(r.metrics);
        if (!m.successRate) return <span style={{ color: '#999' }}>-</span>;
        const pct = (parseFloat(m.successRate) * 100).toFixed(0);
        return <span style={{ color: parseFloat(pct) >= 80 ? '#16a34a' : '#d97706', fontWeight: 600 }}>{pct}%</span>;
      },
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90,
      render: (s: string) => { const t = statusTag[s] ?? { color: 'default', label: s }; return <Tag color={t.color} style={{ borderRadius: 6, fontSize: 11 }}>{t.label}</Tag>; },
    },
    { title: '操作', key: 'action', width: 130,
      render: (_: unknown, r: EvalTask) => (
        <div style={{ display: 'flex', gap: 4 }}>
          {(r.status === 'PENDING' || r.status === 'FAILED') && (
            <Button size="small" type="primary" icon={<PlayCircleOutlined />} onClick={(e) => { e.stopPropagation(); handleExecute(r.id); }}>
              执行
            </Button>
          )}
          {r.status === 'COMPLETED' && (
            <Button size="small" onClick={(e) => { e.stopPropagation(); navigate(`/eval/results/${r.id}`); }}>
              查看结果
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader title="评估任务" actionLabel="新建任务" onAction={() => setCreateOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={tasks.length} label="任务总数" />
          <StatCard value={tasks.filter((t) => t.status === 'COMPLETED').length} label="已完成" />
          <StatCard value={tasks.filter((t) => t.status === 'RUNNING').length} label="运行中" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : tasks.length === 0 ? (
          <EmptyState icon="📊" title="暂无评估任务" description="创建评估任务并执行，衡量 AI 质量" actionLabel="新建任务" onAction={() => setCreateOpen(true)} />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={tasks}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 个任务` }}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>

      <Modal
        title="新建评估任务"
        open={createOpen}
        onOk={handleCreate}
        onCancel={() => setCreateOpen(false)}
        okText="创建"
        cancelText="取消"
        confirmLoading={loading}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="datasetId" label="数据集" rules={[{ required: true, message: '请选择数据集' }]}>
            <Select
              placeholder="选择评估数据集"
              options={datasets.map((d) => ({ value: d.id, label: `${d.name} (${d.evalType})` }))}
            />
          </Form.Item>
          <Form.Item name="targetId" label="目标 ID" rules={[{ required: true, message: '请输入目标 ID' }]}>
            <Input placeholder="Agent/Skill/Knowledge ID" />
          </Form.Item>
          <Form.Item name="targetType" label="目标类型" rules={[{ required: true, message: '请选择目标类型' }]} initialValue="AGENT">
            <Select options={[
              { value: 'AGENT', label: 'Agent' },
              { value: 'SKILL', label: 'Skill' },
              { value: 'RAG', label: 'RAG / Knowledge' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
