import { useEffect } from 'react';
import { Card, Spin, Table, Tag, Button, Space } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { usePlatformStore, typeLabel, typeColor, statusColor, statusLabel } from '../stores/platformStore';
import type { ApprovalRecord } from '../../../shared/api/platform';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function ApprovalPage() {
  const { approvals, loading, fetchApprovals } = usePlatformStore();

  useEffect(() => { fetchApprovals(); }, [fetchApprovals]);

  const columns: ColumnsType<ApprovalRecord> = [
    { title: '类型', dataIndex: 'type', key: 'type', width: 80,
      render: (t: string) => <Tag style={{ borderRadius: 6, fontSize: 11, background: typeColor[t], border: 'none' }}>{typeLabel[t]}</Tag>,
    },
    { title: '对象', dataIndex: 'refName', key: 'refName', width: 160, render: (t: string, r: ApprovalRecord) => <span style={{ fontWeight: 500 }}>{t}<span style={{ fontSize: 11, color: '#999', marginLeft: 4 }}>({r.refType})</span></span> },
    { title: '审批原因', dataIndex: 'reason', key: 'reason' },
    { title: '申请人', dataIndex: 'requester', key: 'requester', width: 90 },
    { title: '审批人', dataIndex: 'approvers', key: 'approvers', width: 120 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (s: string) => <span style={{ fontSize: 12, fontWeight: 500, color: statusColor[s] }}>{statusLabel[s]}</span>,
    },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 140 },
    { title: '操作', key: 'action', width: 140,
      render: (_: unknown, r: ApprovalRecord) => r.status === 'PENDING' ? (
        <Space size="small">
          <Button type="primary" size="small" style={{ fontSize: 11, borderRadius: 6 }}>通过</Button>
          <Button size="small" style={{ fontSize: 11, borderRadius: 6 }} danger>驳回</Button>
        </Space>
      ) : null,
    },
  ];

  return (
    <div>
      <PageHeader title="审批工作台">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={approvals.length} label="审批总数" />
          <StatCard value={approvals.filter((a) => a.status === 'PENDING').length} label="待审批" />
          <StatCard value={approvals.filter((a) => a.status === 'APPROVED').length} label="已通过" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : approvals.length === 0 ? (
          <EmptyState icon="✅" title="暂无审批" description="Human-in-the-Loop 审批任务将在此处理" />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={approvals}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 条审批` }}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>
    </div>
  );
}
