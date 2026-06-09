import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Descriptions } from 'antd';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';

export default function KbDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, loading, fetchDetail } = useKnowledgeStore();

  useEffect(() => {
    if (id) fetchDetail(id);
  }, [id, fetchDetail]);

  if (loading && !detail) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="📚" title="知识库不存在" description="请检查知识库 ID 是否正确" />;

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <Button type="link" onClick={() => navigate('/knowledge')} style={{ padding: 0, marginBottom: 16 }}>
        ← 返回列表
      </Button>

      <Card styles={{ body: { padding: 20 } }} style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <div>
            <h3 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>{detail.name}</h3>
            <p style={{ margin: '4px 0 0', color: '#999', fontSize: 13 }}>{detail.description}</p>
          </div>
          <StatusTag status={detail.status} />
        </div>
        <Descriptions size="small" column={3} bordered>
          <Descriptions.Item label="知识库类型">{detail.kbType}</Descriptions.Item>
          <Descriptions.Item label="Embedding 模型">{detail.embeddingModel}</Descriptions.Item>
          <Descriptions.Item label="文档数">{detail.documentCount}</Descriptions.Item>
          <Descriptions.Item label="Chunk Size">{detail.chunkSize}</Descriptions.Item>
          <Descriptions.Item label="Chunk Overlap">{detail.chunkOverlap}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{detail.createdAt}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="文档管理" styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
        <EmptyState icon="📄" title="暂无文档" description="上传文档开始构建知识库" actionLabel="上传文档" onAction={() => navigate(`/knowledge/${id}/upload`)} />
      </Card>
    </div>
  );
}
