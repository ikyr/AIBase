import { useEffect, useState, type KeyboardEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Descriptions, Input, Select, List, Tag, Modal, Alert } from 'antd';
import { SearchOutlined, FileTextOutlined } from '@ant-design/icons';
import { useKnowledgeStore } from '../stores/knowledgeStore';
import type { SearchHit } from '../../../shared/api/types';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';

const strategyOptions = [
  { value: 'HYBRID', label: '混合检索' },
  { value: 'VECTOR', label: '向量检索' },
  { value: 'KEYWORD', label: '关键词检索' },
];

function scoreColor(score: number): string {
  if (score >= 0.8) return '#16a34a';
  if (score >= 0.6) return '#d97706';
  return '#999';
}

export default function KbDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, loading, fetchDetail, searchResults, searching, searchError, search, clearSearch } = useKnowledgeStore();

  const [query, setQuery] = useState('');
  const [strategy, setStrategy] = useState<'VECTOR' | 'KEYWORD' | 'HYBRID'>('HYBRID');
  const [previewHit, setPreviewHit] = useState<SearchHit | null>(null);

  useEffect(() => {
    if (id) fetchDetail(id);
  }, [id, fetchDetail]);

  const handleSearch = () => {
    const q = query.trim();
    if (!q || !id) return;
    search(id, q, 10, strategy);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch();
  };

  const highlightKeywords = (text: string, keywords: string): string => {
    if (!keywords.trim()) return text;
    try {
      const escaped = keywords.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>');
    } catch {
      return text;
    }
  };

  if (loading && !detail) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="📚" title="知识库不存在" description="请检查知识库 ID 是否正确" />;

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <Button type="link" onClick={() => navigate('/knowledge')} style={{ padding: 0, marginBottom: 16 }}>
        ← 返回列表
      </Button>

      {/* Basic info card */}
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

      {/* Search card */}
      <Card
        title={<span style={{ fontWeight: 600 }}>知识检索</span>}
        styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}
        style={{ marginBottom: 16 }}
      >
        <div style={{ display: 'flex', gap: 10, marginBottom: 16 }}>
          <Input
            prefix={<SearchOutlined style={{ color: '#999' }} />}
            placeholder="输入检索关键词，按 Enter 搜索..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            style={{ flex: 1 }}
            allowClear
            onClear={clearSearch}
          />
          <Select
            value={strategy}
            onChange={(v) => setStrategy(v)}
            options={strategyOptions}
            style={{ width: 130 }}
          />
          <Button type="primary" onClick={handleSearch} loading={searching} icon={<SearchOutlined />}>
            搜索
          </Button>
        </div>

        {searchError && (
          <Alert message={searchError} type="error" showIcon closable style={{ marginBottom: 12 }} />
        )}

        {searching ? (
          <Spin style={{ display: 'block', margin: '24px auto' }} />
        ) : searchResults.length > 0 ? (
          <>
            <div style={{ fontSize: 12, color: '#999', marginBottom: 10 }}>
              共 {searchResults.length} 条结果 · 策略: {strategyOptions.find((s) => s.value === strategy)?.label}
            </div>
            <List
              dataSource={searchResults}
              renderItem={(hit: SearchHit) => (
                <List.Item
                  style={{
                    padding: '12px 16px',
                    borderRadius: 8,
                    border: '1px solid #f0f0f0',
                    marginBottom: 8,
                    background: '#fff',
                    cursor: 'pointer',
                  }}
                  onClick={() => setPreviewHit(hit)}
                >
                  <div style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <FileTextOutlined style={{ color: '#597ef7' }} />
                        <span style={{ fontSize: 12, color: '#999' }}>Chunk {hit.chunkIndex}</span>
                        <Tag style={{ borderRadius: 6, fontSize: 11 }}>
                          相关度: <span style={{ color: scoreColor(hit.score), fontWeight: 600 }}>{(hit.score * 100).toFixed(0)}%</span>
                        </Tag>
                      </div>
                    </div>
                    <div
                      style={{ fontSize: 13, color: '#333', lineHeight: 1.6, maxHeight: 60, overflow: 'hidden' }}
                      dangerouslySetInnerHTML={{ __html: highlightKeywords(hit.content, query) }}
                    />
                  </div>
                </List.Item>
              )}
            />
          </>
        ) : null}
      </Card>

      {/* Document Management */}
      <Card title="文档管理" styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
        <EmptyState icon="📄" title="暂无文档" description="上传文档开始构建知识库" actionLabel="上传文档" onAction={() => navigate(`/knowledge/${id}/upload`)} />
      </Card>

      {/* Content Preview Modal */}
      <Modal
        title={`Chunk ${previewHit?.chunkIndex ?? ''} — 内容详情`}
        open={!!previewHit}
        onCancel={() => setPreviewHit(null)}
        footer={null}
        width={680}
      >
        {previewHit && (
          <div>
            <div style={{ display: 'flex', gap: 16, marginBottom: 16, fontSize: 12, color: '#999' }}>
              <span>Doc ID: {previewHit.docId}</span>
              <span>相关度: <strong style={{ color: scoreColor(previewHit.score) }}>{(previewHit.score * 100).toFixed(1)}%</strong></span>
            </div>
            <div
              style={{
                padding: 16,
                background: '#fafafa',
                borderRadius: 8,
                fontSize: 13,
                lineHeight: 1.7,
                whiteSpace: 'pre-wrap',
                maxHeight: 420,
                overflow: 'auto',
              }}
              dangerouslySetInnerHTML={{ __html: highlightKeywords(previewHit.content, query) }}
            />
          </div>
        )}
      </Modal>
    </div>
  );
}
