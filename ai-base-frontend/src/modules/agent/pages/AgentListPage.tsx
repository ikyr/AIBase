// src/modules/agent/pages/AgentListPage.tsx
import { useEffect } from 'react';
import { Card, Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAgentStore } from '../stores/agentStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function AgentListPage() {
  const { agents, loading, fetchList } = useAgentStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Agent 管理" actionLabel="新建 Agent" onAction={() => {}}>
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
          <EmptyState icon="🤖" title="暂无 Agent" description="创建你的第一个 AI Agent" actionLabel="新建 Agent" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {agents.map((agent) => (
              <Card
                key={agent.id}
                hoverable
                onClick={() => navigate(`/agent/${agent.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{agent.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {agent.description} · {agent.model} · {agent.coordinationMode}模式 · {agent.skillCount}个Skill · {agent.kbCount}个知识库
                    </div>
                  </div>
                  <StatusTag status={agent.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
