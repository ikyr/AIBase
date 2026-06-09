import { useEffect, useState } from 'react';
import { Card, Spin, Tag, Popconfirm } from 'antd';
import { useNavigate } from 'react-router-dom';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useSkillStore, levelLabel, levelColor } from '../stores/skillStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';
import SkillCreateModal from '../components/SkillCreateModal';

export default function SkillListPage() {
  const { skills, loading, fetchList, remove } = useSkillStore();
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Skill 管理" actionLabel="注册 Skill" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={skills.length} label="Skill 总数" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'PROMPT').length} label="Layer 1 Prompt" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'FUNCTION').length} label="Layer 2 Function" />
          <StatCard value={skills.filter((s) => s.skillLevel === 'AGENT').length} label="Layer 3 Agent" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : skills.length === 0 ? (
          <EmptyState icon="🛠" title="暂无 Skill" description="注册你的第一个 AI Skill" actionLabel="注册 Skill" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {skills.map((skill) => (
              <Card key={skill.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }} onClick={() => navigate(`/skill/${skill.id}`)}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{skill.name}</span>
                      <Tag style={{ background: levelColor[skill.skillLevel], border: 'none', borderRadius: 6, fontSize: 11, padding: '1px 8px' }}>
                        {levelLabel[skill.skillLevel]}
                      </Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>{skill.description}</div>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <StatusTag status={skill.status} />
                    <EditOutlined
                      style={{ color: '#1677ff', cursor: 'pointer', fontSize: 14 }}
                      onClick={(e) => { e.stopPropagation(); navigate(`/skill/${skill.id}`); }}
                    />
                    <Popconfirm
                      title="确定删除此 Skill？"
                      onConfirm={async (e) => { e?.stopPropagation(); await remove(skill.id); fetchList(); }}
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

      <SkillCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
