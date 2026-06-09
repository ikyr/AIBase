import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Tag, Timeline } from 'antd';
import { useSkillStore } from '../stores/skillStore';
import EmptyState from '../../../shared/components/EmptyState';

export default function SkillVersionHistory() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { versions, loading, fetchVersions } = useSkillStore();

  useEffect(() => {
    if (id) fetchVersions(id);
  }, [id, fetchVersions]);

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <Button type="link" onClick={() => navigate(`/skill/${id}`)} style={{ padding: 0, marginBottom: 16 }}>
        ← 返回详情
      </Button>

      <Card styles={{ body: { padding: 20 } }}>
        <h3 style={{ margin: '0 0 16px', fontSize: 16, fontWeight: 700 }}>版本历史</h3>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : versions.length === 0 ? (
          <EmptyState icon="📜" title="暂无版本记录" />
        ) : (
          <Timeline
            items={versions.map((v) => ({
              color: v.status === 'PUBLISHED' ? '#16a34a' : '#d97706',
              children: (
                <div key={v.id}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <span style={{ fontWeight: 600, fontSize: 14 }}>{v.version}</span>
                    <Tag style={{ borderRadius: 6, fontSize: 11 }}>{v.status}</Tag>
                  </div>
                  <div style={{ fontSize: 13, color: '#555', marginBottom: 4 }}>{v.changelog}</div>
                  <div style={{ fontSize: 12, color: '#999' }}>{v.createdAt}</div>
                </div>
              ),
            }))}
          />
        )}
      </Card>
    </div>
  );
}
