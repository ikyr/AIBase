import { useEffect, useState } from 'react';
import { Card, Spin, Tag, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useModelStore } from '../stores/modelStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';
import ModelCreateModal from '../components/ModelCreateModal';
import type { ModelCreateRequest } from '../../../shared/api/model';

export default function ModelListPage() {
  const { models, loading, fetchList, remove } = useModelStore();
  const [modalOpen, setModalOpen] = useState(false);
  const [editData, setEditData] = useState<(ModelCreateRequest & { id?: string }) | undefined>();

  useEffect(() => { fetchList(); }, [fetchList]);

  const handleEdit = (m: typeof models[0]) => {
    setEditData({
      id: m.id,
      name: m.name,
      provider: m.provider,
      endpoint: m.endpoint,
      apiKeyRef: m.apiKeyRef,
      maxTokens: m.maxTokens,
      capabilities: m.capabilities,
      priority: m.priority,
    });
    setModalOpen(true);
  };

  const handleClose = () => {
    setModalOpen(false);
    setEditData(undefined);
  };

  return (
    <div>
      <PageHeader title="模型管理" actionLabel="添加模型" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={models.length} label="模型总数" />
          <StatCard value={models.filter((m) => m.status === 'ACTIVE').length} label="启用中" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : models.length === 0 ? (
          <EmptyState icon="🧠" title="暂无模型" description="添加你的第一个 AI 模型" actionLabel="添加模型" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {models.map((m) => (
              <Card key={m.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{m.name}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{m.provider}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {m.endpoint || '默认端点'} · 优先级: {m.priority} · {m.capabilities || '无'}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <StatusTag status={m.status} />
                    <EditOutlined
                      style={{ color: '#1677ff', cursor: 'pointer', fontSize: 14 }}
                      onClick={() => handleEdit(m)}
                    />
                    <Popconfirm
                      title="确定删除此模型？"
                      onConfirm={async (e) => { e?.stopPropagation(); await remove(m.id); fetchList(); }}
                    >
                      <DeleteOutlined
                        style={{ color: '#ff4d4f', cursor: 'pointer', fontSize: 14 }}
                        onClick={(e) => e.stopPropagation()}
                      />
                    </Popconfirm>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <ModelCreateModal open={modalOpen} editData={editData} onClose={handleClose} />
    </div>
  );
}
