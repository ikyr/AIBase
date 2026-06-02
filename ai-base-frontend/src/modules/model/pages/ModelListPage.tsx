// src/modules/model/pages/ModelListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useModelStore } from '../stores/modelStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function ModelListPage() {
  const { models, loading, fetchList } = useModelStore();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="模型管理" actionLabel="添加模型" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={models.length} label="模型总数" />
          <StatCard value={models.filter((m) => m.status === 'ACTIVE').length} label="启用中" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : models.length === 0 ? (
          <EmptyState icon="🧠" title="暂无模型" description="添加你的第一个 AI 模型" actionLabel="添加模型" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {models.map((m) => (
              <Card key={m.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{m.name}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{m.provider}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {m.endpoint} · 优先级: {m.priority} · {m.capabilities.join(', ')}
                    </div>
                  </div>
                  <StatusTag status={m.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
