// src/modules/skill/pages/SkillListPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useSkillStore, levelLabel, levelColor } from '../stores/skillStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

export default function SkillListPage() {
  const { skills, loading, fetchList } = useSkillStore();
  const navigate = useNavigate();

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="Skill 管理" actionLabel="注册 Skill" onAction={() => {}}>
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
          <EmptyState icon="🛠" title="暂无 Skill" description="注册你的第一个 AI Skill" actionLabel="注册 Skill" onAction={() => {}} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {skills.map((skill) => (
              <Card
                key={skill.id}
                hoverable
                onClick={() => navigate(`/skill/${skill.id}`)}
                styles={{ body: { padding: 16 } }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{skill.name}</span>
                      <Tag style={{ background: levelColor[skill.skillLevel], border: 'none', borderRadius: 6, fontSize: 11, padding: '1px 8px' }}>
                        {levelLabel[skill.skillLevel]}
                      </Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>{skill.description}</div>
                  </div>
                  <StatusTag status={skill.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
