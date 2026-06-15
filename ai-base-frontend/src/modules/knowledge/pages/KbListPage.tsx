// src/modules/knowledge/pages/KbListPage.tsx
import { useEffect, useState } from 'react';
import { Spin, Alert, Button, Card, Popconfirm } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import KbCreateModal from '../components/KbCreateModal';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function KbListPage() {
  const { kbs, loading, error, fetchList, remove } = useKnowledgeStore();
  const [modalOpen, setModalOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="知识库" actionLabel="新建知识库" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={kbs.length} label="知识库总数" />
          <StatCard value={kbs.filter((k) => k.status === 'ACTIVE').length} label="运行中" />
          <StatCard value={kbs.reduce((sum, k) => sum + (k.documentCount || 0), 0)} label="文档总数" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 12 }} closable />}
        {loading && kbs.length === 0 ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : kbs.length === 0 ? (
          <EmptyState icon="📚" title="暂无知识库" description="创建你的第一个知识库" actionLabel="新建知识库" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {kbs.map((kb) => (
              <Card
                key={kb.id}
                hoverable
                onClick={() => navigate(`/knowledge/${kb.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{kb.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {kb.description || '无描述'} · {kb.kbType} · Chunk {kb.chunkSize}/{kb.chunkOverlap} · Docs: {kb.documentCount}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <StatusTag status={kb.status} />
                    <Popconfirm title="确定删除此知识库？" onConfirm={() => remove(kb.id)}>
                      <Button size="small" danger icon={<DeleteOutlined />} onClick={(e) => e.stopPropagation()} />
                    </Popconfirm>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <KbCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
