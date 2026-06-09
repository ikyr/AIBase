import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Tag, Button, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useSkillStore, levelLabel, levelColor } from '../stores/skillStore';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import SkillCreateModal from '../components/SkillCreateModal';
import type { SkillCreateRequest } from '../../../shared/api/skill';

export default function SkillDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, loading, fetchDetail, remove } = useSkillStore();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    if (id) fetchDetail(id);
  }, [id, fetchDetail]);

  if (loading) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="🛠" title="Skill 不存在" description="请检查 Skill ID 是否正确" />;

  const editData: SkillCreateRequest & { id?: string } = {
    id: detail.id,
    name: detail.name,
    description: detail.description,
    tags: detail.tags,
    skillLevel: detail.skillLevel,
    promptTemplate: detail.promptTemplate,
    params: detail.params,
    inputSchema: detail.inputSchema,
    outputSchema: detail.outputSchema,
    executionMode: detail.executionMode,
    timeoutMs: detail.timeoutMs,
    agentRefId: detail.agentRefId,
  };

  const handleDelete = async () => {
    if (id) {
      await remove(id);
      navigate('/skill');
    }
  };

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Button type="link" onClick={() => navigate('/skill')} style={{ padding: 0 }}>
          ← 返回列表
        </Button>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button icon={<EditOutlined />} onClick={() => setModalOpen(true)}>编辑</Button>
          <Popconfirm title="确定删除此 Skill？" onConfirm={handleDelete}>
            <Button danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </div>
      </div>

      <Card styles={{ body: { padding: 20 } }} style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <div>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>
              {detail.name}
              <Tag style={{ marginLeft: 8, borderRadius: 6, fontSize: 11, background: levelColor[detail.skillLevel], border: 'none' }}>
                {levelLabel[detail.skillLevel]}
              </Tag>
            </h3>
            <p style={{ margin: '4px 0 0', color: '#999', fontSize: 13 }}>{detail.description}</p>
          </div>
          <StatusTag status={detail.status} />
        </div>
        <div style={{ fontSize: 12, color: '#999' }}>
          创建于 {detail.createdAt} · 更新于 {detail.updatedAt}
        </div>
      </Card>

      {detail.promptTemplate && (
        <Card title="Prompt 模板" styles={{ body: { padding: 16 }, header: { padding: '12px 20px', fontWeight: 600 } }} style={{ marginBottom: 16 }}>
          <pre style={{ margin: 0, fontSize: 12, lineHeight: 1.6, whiteSpace: 'pre-wrap', background: '#fafafa', padding: 12, borderRadius: 8 }}>
            {detail.promptTemplate}
          </pre>
        </Card>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <Card title="输入 Schema" styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
          {detail.inputSchema ? (
            <pre style={{ margin: 0, fontSize: 12, lineHeight: 1.6, whiteSpace: 'pre-wrap', background: '#fafafa', padding: 12, borderRadius: 8 }}>
              {detail.inputSchema}
            </pre>
          ) : (
            <div style={{ color: '#999', fontSize: 13 }}>未定义</div>
          )}
        </Card>
        <Card title="输出 Schema" styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
          {detail.outputSchema ? (
            <pre style={{ margin: 0, fontSize: 12, lineHeight: 1.6, whiteSpace: 'pre-wrap', background: '#fafafa', padding: 12, borderRadius: 8 }}>
              {detail.outputSchema}
            </pre>
          ) : (
            <div style={{ color: '#999', fontSize: 13 }}>未定义</div>
          )}
        </Card>
      </div>

      <SkillCreateModal open={modalOpen} editData={editData} onClose={() => setModalOpen(false)} />
    </div>
  );
}
