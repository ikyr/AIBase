import { useEffect } from 'react';
import { Card, Spin, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useAgentStore } from '../stores/agentStore';
import type { AgentSession } from '../../../shared/api/agent';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const columns: ColumnsType<AgentSession> = [
  { title: '会话标题', dataIndex: 'title', key: 'title', render: (t: string) => <span style={{ fontWeight: 500 }}>{t || '未命名会话'}</span> },
  { title: 'Agent ID', dataIndex: 'agentId', key: 'agentId', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '开始时间', dataIndex: 'startedAt', key: 'startedAt', width: 160 },
];

export default function SessionHistoryPage() {
  const { sessions, loading, fetchSessions } = useAgentStore();

  useEffect(() => { fetchSessions(); }, [fetchSessions]);

  return (
    <div>
      <PageHeader title="会话记录">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={sessions.length} label="会话总数" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : sessions.length === 0 ? (
          <EmptyState icon="💬" title="暂无会话记录" description="Agent 会话记录将在此展示" />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={sessions}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 条` }}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>
    </div>
  );
}
