import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEvalStore } from '../stores/evalStore';
import type { EvalResult } from '../../../shared/api/eval';
import EmptyState from '../../../shared/components/EmptyState';

export default function EvalResultPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { results, loading, fetchResults } = useEvalStore();

  useEffect(() => {
    if (id) fetchResults(id);
  }, [id, fetchResults]);

  if (loading) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!results.length) return <EmptyState icon="📊" title="评估结果不存在" description="请检查任务 ID 是否正确" />;

  const passedCount = results.filter((r) => r.passed).length;
  const failedCount = results.filter((r) => !r.passed).length;

  const detailCols: ColumnsType<EvalResult> = [
    { title: '项目 ID', dataIndex: 'itemId', key: 'itemId', width: 100 },
    { title: '实际输出', dataIndex: 'actualOutput', key: 'actualOutput', ellipsis: true },
    { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 80, align: 'center',
      render: (ms: number) => <span>{ms}ms</span>,
    },
    { title: '结果', dataIndex: 'passed', key: 'passed', width: 70, align: 'center',
      render: (p: boolean) => <Tag color={p ? 'success' : 'error'} style={{ borderRadius: 6, fontSize: 11 }}>{p ? '通过' : '失败'}</Tag>,
    },
  ];

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <Button type="link" onClick={() => navigate('/eval/tasks')} style={{ padding: 0, marginBottom: 16 }}>
        ← 返回任务列表
      </Button>

      <Card styles={{ body: { padding: 20 } }} style={{ marginBottom: 16 }}>
        <h3 style={{ margin: '0 0 4px', fontSize: 18, fontWeight: 700 }}>评估结果</h3>
        <p style={{ margin: '0 0 16px', color: '#999', fontSize: 13 }}>任务 ID: {id} · 共 {results.length} 项</p>

        <div style={{ display: 'flex', gap: 12 }}>
          <Tag color="success" style={{ borderRadius: 6 }}>通过: {passedCount}</Tag>
          <Tag color="error" style={{ borderRadius: 6 }}>失败: {failedCount}</Tag>
          <Tag style={{ borderRadius: 6 }}>总计: {results.length}</Tag>
        </div>
      </Card>

      <Card title="评估明细" styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
        <Table columns={detailCols} dataSource={results} rowKey="id" pagination={{ pageSize: 20, size: 'small' }} size="small" style={{ fontSize: 13 }} />
      </Card>
    </div>
  );
}
