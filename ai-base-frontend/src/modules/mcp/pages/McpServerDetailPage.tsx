import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Spin, Button, Descriptions, Table, Popconfirm } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useMcpStore } from '../stores/mcpStore';
import { listMcpServerTools, type McpTool, type McpCreateRequest } from '../../../shared/api/mcp';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import McpCreateModal from '../components/McpCreateModal';

const healthLabel: Record<string, string> = { HEALTHY: '健康', DEGRADED: '降级', UNHEALTHY: '异常', UNKNOWN: '未知' };

const toolCols: ColumnsType<McpTool> = [
  { title: '工具名称', dataIndex: 'name', key: 'name', render: (t: string) => <code style={{ fontSize: 12, fontWeight: 500 }}>{t}</code> },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 70, render: (s: string) => <StatusTag status={s} /> },
];

export default function McpServerDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, loading, fetchDetail, remove } = useMcpStore();
  const [tools, setTools] = useState<McpTool[]>([]);
  const [toolsLoading, setToolsLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    if (id) {
      fetchDetail(id);
      setToolsLoading(true);
      listMcpServerTools(id).then((res) => {
        setTools(res.success ? (res.data ?? []) : []);
        setToolsLoading(false);
      });
    }
  }, [id, fetchDetail]);

  if (loading) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="🔌" title="Server 不存在" description="请检查 Server ID 是否正确" />;

  const editData: McpCreateRequest & { id?: string } = {
    id: detail.id,
    name: detail.name,
    description: detail.description,
    serverType: detail.serverType,
    transport: detail.transport,
    endpoint: detail.endpoint,
  };

  const handleDelete = async () => {
    if (id) {
      await remove(id);
      navigate('/mcp');
    }
  };

  return (
    <div style={{ padding: '0 20px 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Button type="link" onClick={() => navigate('/mcp')} style={{ padding: 0 }}>
          ← 返回列表
        </Button>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button icon={<EditOutlined />} onClick={() => setModalOpen(true)}>编辑</Button>
          <Popconfirm title="确定删除此 MCP Server？" onConfirm={handleDelete}>
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
          <div style={{ display: 'flex', gap: 6 }}>
            <StatusTag status={detail.healthStatus} />
            <StatusTag status={detail.status} />
          </div>
        </div>
        <Descriptions size="small" column={3} bordered>
          <Descriptions.Item label="类型">{detail.serverType}</Descriptions.Item>
          <Descriptions.Item label="传输协议">{detail.transport}</Descriptions.Item>
          <Descriptions.Item label="健康状态">{healthLabel[detail.healthStatus] ?? detail.healthStatus}</Descriptions.Item>
          <Descriptions.Item label="端点">{detail.endpoint}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title={`注册工具 (${tools.length})`} styles={{ header: { padding: '12px 20px', fontWeight: 600 } }}>
        {toolsLoading ? (
          <Spin style={{ display: 'block', margin: '20px auto' }} />
        ) : tools.length === 0 ? (
          <EmptyState icon="🔧" title="暂无注册工具" />
        ) : (
          <Table columns={toolCols} dataSource={tools} rowKey="id" pagination={false} size="small" style={{ fontSize: 13 }} />
        )}
      </Card>

      <McpCreateModal open={modalOpen} editData={editData} onClose={() => setModalOpen(false)} />
    </div>
  );
}
