// src/modules/eval/pages/EvalDatasetListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useEvalStore } from '../stores/evalStore';
import StatCard from '../../../shared/components/StatCard';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function EvalDatasetListPage() {
  const { datasets, loading, fetchList } = useEvalStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="评估数据集" actionLabel="新建数据集" onAction={() => {}}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={datasets.length} label="数据集总数" />
          <StatCard value={datasets.reduce((s, d) => s + d.itemCount, 0)} label="评估条目" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : datasets.length === 0 ? (
          <EmptyState icon="📊" title="暂无评估数据集" description="创建评估数据集来衡量 AI 质量" actionLabel="新建数据集" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {datasets.map((ds) => (
              <Card key={ds.id} hoverable onClick={() => navigate(`/eval/results/${ds.id}`)} styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{ds.name}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{ds.evalType}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>{ds.itemCount} 条评估项 · 创建于 {ds.createdAt}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
