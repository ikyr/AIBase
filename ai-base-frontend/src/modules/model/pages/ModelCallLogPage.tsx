import { useEffect } from 'react';
import { Card, Spin, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useModelStore } from '../stores/modelStore';
import type { ModelCallLog } from '../../../shared/api/model';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const statusColor: Record<string, string> = { SUCCESS: 'success', ERROR: 'error', TIMEOUT: 'warning' };

const columns: ColumnsType<ModelCallLog> = [
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '模型', dataIndex: 'modelName', key: 'modelName', width: 110, render: (t: string) => <Tag style={{ borderRadius: 6, fontSize: 11 }}>{t}</Tag> },
  { title: '调用方', dataIndex: 'callerRef', key: 'callerRef', width: 140 },
  { title: '输入 Token', dataIndex: 'inputTokens', key: 'inputTokens', width: 90, align: 'right', render: (v: number) => v.toLocaleString() },
  { title: '输出 Token', dataIndex: 'outputTokens', key: 'outputTokens', width: 90, align: 'right', render: (v: number) => v.toLocaleString() },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 80, align: 'center', render: (ms: number) => `${ms}ms` },
  { title: '费用', dataIndex: 'cost', key: 'cost', width: 70, align: 'right' },
  { title: '状态', dataIndex: 'callStatus', key: 'callStatus', width: 70, align: 'center',
    render: (s: string) => <Tag color={statusColor[s] ?? 'default'} style={{ borderRadius: 6, fontSize: 11 }}>{s}</Tag>,
  },
];

export default function ModelCallLogPage() {
  const { logs, loading, fetchLogs } = useModelStore();

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  const totalTokens = logs.reduce((s, l) => s + l.inputTokens + l.outputTokens, 0);
  const totalCost = logs.reduce((s, l) => s + parseFloat(l.cost?.replace('¥', '') || '0'), 0);

  return (
    <div>
      <PageHeader title="调用日志">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={logs.length} label="调用次数" />
          <StatCard value={totalTokens.toLocaleString()} label="Token 消耗" />
          <StatCard value={`¥${totalCost.toFixed(3)}`} label="累计费用" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : logs.length === 0 ? (
          <EmptyState icon="📋" title="暂无调用日志" description="模型调用记录将在此展示" />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={logs}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 条日志` }}
              style={{ fontSize: 13 }}
              size="small"
            />
          </Card>
        )}
      </div>
    </div>
  );
}
