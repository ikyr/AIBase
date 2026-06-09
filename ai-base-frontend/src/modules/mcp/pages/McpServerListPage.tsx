import { useEffect, useState } from 'react';
import { Card, Spin, Popconfirm } from 'antd';
import { useNavigate } from 'react-router-dom';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useMcpStore } from '../stores/mcpStore';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';
import McpCreateModal from '../components/McpCreateModal';

const healthLabel: Record<string, string> = { HEALTHY: '健康', DEGRADED: '降级', UNHEALTHY: '异常', UNKNOWN: '未知' };

export default function McpServerListPage() {
  const { servers, loading, fetchList, remove } = useMcpStore();
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => { fetchList(); }, [fetchList]);

  return (
    <div>
      <PageHeader title="MCP Server 管理" actionLabel="注册 Server" onAction={() => setModalOpen(true)}>
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={servers.length} label="Server 总数" />
          <StatCard value={servers.filter((s) => s.healthStatus === 'HEALTHY').length} label="健康" />
        </div>
      </PageHeader>

      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : servers.length === 0 ? (
          <EmptyState icon="🔌" title="暂无 MCP Server" description="注册你的第一个 MCP Server" actionLabel="注册 Server" onAction={() => setModalOpen(true)} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {servers.map((srv) => (
              <Card key={srv.id} hoverable styles={{ body: { padding: 16 } }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }} onClick={() => navigate(`/mcp/${srv.id}`)}>
                    <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>{srv.name}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {srv.serverType} · {srv.transport} · 健康: {healthLabel[srv.healthStatus] ?? srv.healthStatus}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <StatusTag status={srv.healthStatus} />
                    <StatusTag status={srv.status} />
                    <EditOutlined
                      style={{ color: '#1677ff', cursor: 'pointer', fontSize: 14 }}
                      onClick={(e) => { e.stopPropagation(); navigate(`/mcp/${srv.id}`); }}
                    />
                    <Popconfirm
                      title="确定删除此 MCP Server？"
                      onConfirm={async (e) => { e?.stopPropagation(); await remove(srv.id); fetchList(); }}
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

      <McpCreateModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
