import { useEffect, useState } from 'react';
import { Card, Spin, Popconfirm } from 'antd';
import { useNavigate } from 'react-router-dom';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useWorkflowStore } from '../stores/workflowStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';
import WorkflowCreateModal from '../components/WorkflowCreateModal';

export default function WorkflowListPage() {
  const { workflows, loading, fetchList, remove } = useWorkflowStore();
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => { fetchList(); }, [fetchList]);

  const handleCreateAndEdit = async () => {
    setModalOpen(true);
  };

  return (
    <div>
      <PageHeader title="工作流管理" actionLabel="新建工作流" onAction={handleCreateAndEdit}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={workflows.length} label="工作流总数" />
          <StatCard value={workflows.filter((w) => w.status === 'ACTIVE').length} label="活跃" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : workflows.length === 0 ? (
          <EmptyState icon="⚙️" title="暂无工作流" description="创建你的第一个工作流" actionLabel="新建工作流" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {workflows.map((wf) => (
              <Card key={wf.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }} onClick={() => navigate(`/workflow/${wf.id}`)}>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{wf.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {wf.description} · v{wf.version} · 更新于 {wf.updatedAt}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <StatusTag status={wf.status} />
                    <EditOutlined
                      style={{ color: '#1677ff', cursor: 'pointer', fontSize: 14 }}
                      onClick={(e) => { e.stopPropagation(); navigate(`/workflow/${wf.id}`); }}
                    />
                    <Popconfirm
                      title="确定删除此工作流？"
                      onConfirm={async (e) => { e?.stopPropagation(); await remove(wf.id); fetchList(); }}
                      onCancel={(e) => e?.stopPropagation()}
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

      <WorkflowCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
