// src/modules/platform/pages/PromptVersionPage.tsx
import { useEffect } from 'react';
import { Card, Spin, Tag } from 'antd';
import { usePlatformStore } from '../stores/platformStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const refLabel: Record<string, string> = { AGENT: 'Agent', SKILL: 'Skill', WORKFLOW: 'Workflow' };

export default function PromptVersionPage() {
  const { prompts, loading, fetchPrompts } = usePlatformStore();

  useEffect(() => { fetchPrompts(); }, [fetchPrompts]);

  return (
    <div>
      <PageHeader title="Prompt 版本管理">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={prompts.length} label="Prompt 总数" />
          <StatCard value={prompts.filter((p) => p.status === 'PUBLISHED').length} label="已发布" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : prompts.length === 0 ? (
          <EmptyState icon="📋" title="暂无 Prompt 版本" description="Prompt 版本将在创建 Agent/Skill 时自动生成" />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {prompts.map((p) => (
              <Card key={p.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 14, fontWeight: 600 }}>{refLabel[p.refType]} Prompt v{p.version}</span>
                      <Tag style={{ borderRadius: 6, fontSize: 11 }}>{p.refType}</Tag>
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>ref: {p.refId} · {p.createdAt} · by {p.createdBy}</div>
                  </div>
                  <StatusTag status={p.status} />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
