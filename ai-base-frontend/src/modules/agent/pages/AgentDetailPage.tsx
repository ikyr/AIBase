import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Descriptions, Button, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useAgentStore } from '../stores/agentStore';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import AgentCreateModal from '../components/AgentCreateModal';
import type { AgentCreateRequest } from '../../../shared/api/agent';

const modeLabel: Record<string, string> = { GRAPH: 'Graph 编排', REACT: 'ReAct 推理', NEGOTIATION: '协商' };

export default function AgentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, loading, fetchDetail, remove } = useAgentStore();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    if (id) fetchDetail(id);
  }, [id, fetchDetail]);

  if (loading) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="🤖" title="Agent 不存在" description="请检查 Agent ID 是否正确" />;

  const skillIds = detail.skillIds ? detail.skillIds.split(',').filter(Boolean) : [];
  const kbIds = detail.kbIds ? detail.kbIds.split(',').filter(Boolean) : [];

  const editData: AgentCreateRequest & { id?: string } = {
    id: detail.id,
    name: detail.name,
    description: detail.description,
    systemPrompt: detail.systemPrompt,
    model: detail.model,
    tools: detail.tools,
    skillIds: detail.skillIds,
    kbIds: detail.kbIds,
    coordinationMode: detail.coordinationMode,
    constraints: detail.constraints,
  };

  const handleDelete = async () => {
    if (id) {
      await remove(id);
      navigate('/agent');
    }
  };

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Button type="link" onClick={() => navigate('/agent')} style={{ padding: 0 }}>
          ← 返回列表
        </Button>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button icon={<EditOutlined />} onClick={() => setModalOpen(true)}>编辑</Button>
          <Popconfirm title="确定删除此 Agent？" onConfirm={handleDelete}>
            <Button danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </div>
      </div>

      <Card styles={{ body: { padding: 20 } }} style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <div>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>{detail.name}</h3>
            <p style={{ margin: '4px 0 0', color: '#999', fontSize: 13 }}>{detail.description}</p>
          </div>
          <StatusTag status={detail.status} />
        </div>
        <Descriptions size="small" column={3} bordered>
          <Descriptions.Item label="模型">{detail.model}</Descriptions.Item>
          <Descriptions.Item label="编排模式">{modeLabel[detail.coordinationMode] ?? detail.coordinationMode}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{detail.createdAt}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="System Prompt" styles={{ body: { padding: 16 }, header: { padding: '12px 20px', fontWeight: 600 } }} style={{ marginBottom: 16 }}>
        <pre style={{ margin: 0, fontSize: 12, lineHeight: 1.6, whiteSpace: 'pre-wrap', background: '#fafafa', padding: 12, borderRadius: 8 }}>
          {detail.systemPrompt || '无'}
        </pre>
      </Card>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <Card title={`关联 Skill (${skillIds.length})`} styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
          {skillIds.length === 0 ? (
            <div style={{ color: '#999', fontSize: 13 }}>无关联 Skill</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {skillIds.map((sid) => (
                <div key={sid} style={{ padding: '8px 12px', background: '#fafafa', borderRadius: 8, fontSize: 13, fontWeight: 500 }}>
                  {sid}
                </div>
              ))}
            </div>
          )}
        </Card>
        <Card title={`关联知识库 (${kbIds.length})`} styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
          {kbIds.length === 0 ? (
            <div style={{ color: '#999', fontSize: 13 }}>无关联知识库</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {kbIds.map((kbid) => (
                <div key={kbid} style={{ padding: '8px 12px', background: '#fafafa', borderRadius: 8, fontSize: 13, fontWeight: 500 }}>
                  {kbid}
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      <AgentCreateModal open={modalOpen} editData={editData} onClose={() => setModalOpen(false)} />
    </div>
  );
}
