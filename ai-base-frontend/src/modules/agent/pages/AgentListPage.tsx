import { useEffect, useState } from 'react';
import { Card, Spin, Popconfirm } from 'antd';
import { useNavigate } from 'react-router-dom';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useAgentStore } from '../stores/agentStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';
import AgentCreateModal from '../components/AgentCreateModal';

export default function AgentListPage() {
  const { agents, loading, fetchList, remove } = useAgentStore();
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Agent 管理" actionLabel="新建 Agent" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={agents.length} label="Agent 总数" />
          <StatCard value={agents.filter((a) => a.status === 'ACTIVE').length} label="运行中" />
          <StatCard value="1,247" label="今日会话" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : agents.length === 0 ? (
          <EmptyState icon="🤖" title="暂无 Agent" description="创建你的第一个 AI Agent" actionLabel="新建 Agent" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {agents.map((agent) => {
              const skillCount = agent.skillIds ? agent.skillIds.split(',').filter(Boolean).length : 0;
              const kbCount = agent.kbIds ? agent.kbIds.split(',').filter(Boolean).length : 0;
              return (
                <Card
                  key={agent.id}
                  hoverable
                  styles={{ body: { padding: 16 } }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }} onClick={() => navigate(`/agent/${agent.id}`)}>
                      <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{agent.name}</div>
                      <div style={{ fontSize: 12, color: '#999' }}>
                        {agent.description} · {agent.model} · {agent.coordinationMode}模式 · {skillCount}个Skill · {kbCount}个知识库
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                      <StatusTag status={agent.status} />
                      <EditOutlined
                        style={{ color: '#1677ff', cursor: 'pointer', fontSize: 14 }}
                        onClick={(e) => { e.stopPropagation(); navigate(`/agent/${agent.id}`); }}
                      />
                      <Popconfirm
                        title="确定删除此 Agent？"
                        onConfirm={async (e) => { e?.stopPropagation(); await remove(agent.id); fetchList(); }}
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
              );
            })}
          </div>
        )}
      </div>

      <AgentCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
