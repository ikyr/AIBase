import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useWorkflowStore } from '../stores/workflowStore';
import type { WfInstance } from '../../../shared/api/workflow';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';

const statusTag: Record<string, { color: string; label: string }> = {
  RUNNING: { color: 'processing', label: '运行中' },
  COMPLETED: { color: 'success', label: '已完成' },
  FAILED: { color: 'error', label: '失败' },
  CANCELED: { color: 'default', label: '已取消' },
};

export default function WorkflowInstancePage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { instances, loading, fetchInstances } = useWorkflowStore();

  useEffect(() => { fetchInstances(); }, [fetchInstances]);

  const filtered = id ? instances.filter((i) => i.definitionId === id) : instances;

  const columns: ColumnsType<WfInstance> = [
    { title: '工作流 ID', dataIndex: 'definitionId', key: 'definitionId', render: (t: string) => <span style={{ fontWeight: 500 }}>{t.slice(0, 8)}...</span> },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (s: string) => { const t = statusTag[s] ?? { color: 'default', label: s }; return <Tag color={t.color} style={{ borderRadius: 6, fontSize: 11 }}>{t.label}</Tag>; },
    },
    { title: '开始时间', dataIndex: 'startedAt', key: 'startedAt', width: 160 },
    { title: '完成时间', dataIndex: 'completedAt', key: 'completedAt', width: 160 },
    { title: 'Trace ID', dataIndex: 'traceId', key: 'traceId', width: 120, render: (t: string) => t ? <code style={{ fontSize: 11 }}>{t.slice(0, 12)}...</code> : '-' },
  ];

  return (
    <div>
      <div style={{ padding: '0 20px' }}>
        <Button type="link" onClick={() => navigate('/workflow')} style={{ padding: 0, marginBottom: 16 }}>
          ← 返回工作流列表
        </Button>
        <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
          <StatCard value={filtered.length} label="实例总数" />
          <StatCard value={filtered.filter((i) => i.status === 'COMPLETED').length} label="已完成" />
          <StatCard value={filtered.filter((i) => i.status === 'RUNNING').length} label="运行中" />
          <StatCard value={filtered.filter((i) => i.status === 'FAILED').length} label="失败" />
        </div>
      </div>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : filtered.length === 0 ? (
          <EmptyState icon="📋" title="暂无运行实例" description="工作流运行实例将在此展示" />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={filtered}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 条记录` }}
              style={{ fontSize: 13 }}
              size="small"
            />
          </Card>
        )}
      </div>
    </div>
  );
}
