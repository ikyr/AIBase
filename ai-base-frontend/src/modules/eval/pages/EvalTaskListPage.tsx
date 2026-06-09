import { useEffect } from 'react';
import { Card, Spin, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { useEvalStore } from '../stores/evalStore';
import type { EvalTask } from '../../../shared/api/eval';
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
  const { tasks, loading, fetchTasks } = useEvalStore();
  const navigate = useNavigate();

  useEffect(() => { fetchTasks(); }, [fetchTasks]);

  const columns: ColumnsType<EvalTask> = [
    { title: '任务 ID', dataIndex: 'id', key: 'id', width: 100, render: (t: string) => <span style={{ fontWeight: 500 }}>{t.slice(0, 8)}...</span> },
    { title: '数据集', dataIndex: 'datasetId', key: 'datasetId', width: 120 },
    { title: '目标', dataIndex: 'targetId', key: 'targetId', width: 120, render: (t: string) => <span>{t}</span> },
    { title: '通过/总数', key: 'progress', width: 100, align: 'center',
      render: (_: unknown, r: EvalTask) => <span>{r.passedItems} / {r.totalItems}</span>,
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90,
      render: (s: string) => { const t = statusTag[s] ?? { color: 'default', label: s }; return <Tag color={t.color} style={{ borderRadius: 6, fontSize: 11 }}>{t.label}</Tag>; },
    },
    { title: '开始时间', dataIndex: 'startedAt', key: 'startedAt', width: 160 },
  ];

  return (
    <div>
      <PageHeader title="评估任务" actionLabel="新建任务" onAction={() => {}}>
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
          <EmptyState icon="📊" title="暂无评估任务" description="运行评估任务来衡量 Agent 质量" actionLabel="新建任务" onAction={() => {}} />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={tasks}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 个任务` }}
              onRow={(r) => ({ onClick: () => r.status === 'COMPLETED' && navigate(`/eval/results/${r.id}`), style: { cursor: r.status === 'COMPLETED' ? 'pointer' : 'default' } })}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>
    </div>
  );
}
