import { useEffect } from 'react';
import { Card, Spin, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useModelStore } from '../stores/modelStore';
import type { ModelRouteRule } from '../../../shared/api/model';
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
  const { rules, loading, fetchRules } = useModelStore();

  useEffect(() => { fetchRules(); }, [fetchRules]);

  return (
    <div>
      <PageHeader title="路由规则" actionLabel="新建规则" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={rules.length} label="规则总数" />
          <StatCard value={rules.filter((r) => r.status === 'ACTIVE').length} label="已启用" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : rules.length === 0 ? (
          <EmptyState icon="🔀" title="暂无路由规则" description="配置路由规则来分流模型调用" actionLabel="新建规则" onAction={() => {}} />
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
    </div>
  );
}
