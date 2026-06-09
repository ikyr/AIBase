import { useEffect } from 'react';
import { Card, Spin, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMcpStore } from '../stores/mcpStore';
import type { McpTool } from '../../../shared/api/mcp';
import StatCard from '../../../shared/components/StatCard';
import StatusTag from '../../../shared/components/StatusTag';
import EmptyState from '../../../shared/components/EmptyState';
import PageHeader from '../../../shared/components/PageHeader';

const columns: ColumnsType<McpTool> = [
  { title: '工具名称', dataIndex: 'name', key: 'name', width: 180, render: (t: string) => <code style={{ fontSize: 12, fontWeight: 500 }}>{t}</code> },
  { title: '所属 Server', dataIndex: 'serverId', key: 'serverId', width: 150 },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 70, render: (s: string) => <StatusTag status={s} /> },
];

export default function McpToolListPage() {
  const { tools, loading, fetchAllTools } = useMcpStore();

  useEffect(() => { fetchAllTools(); }, [fetchAllTools]);

  return (
    <div>
      <PageHeader title="MCP 工具列表">
        <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
          <StatCard value={tools.length} label="工具总数" />
          <StatCard value={tools.filter((t) => t.status === 'ACTIVE').length} label="活跃工具" />
        </div>
      </PageHeader>
      <div style={{ padding: '0 20px 20px' }}>
        {loading ? (
          <Spin style={{ display: 'block', margin: '40px auto' }} />
        ) : tools.length === 0 ? (
          <EmptyState icon="🔧" title="暂无工具" description="注册 MCP Server 后工具将自动注册" />
        ) : (
          <Card styles={{ body: { padding: 0 } }}>
            <Table
              columns={columns}
              dataSource={tools}
              rowKey="id"
              pagination={{ pageSize: 10, size: 'small', showTotal: (t) => `共 ${t} 个工具` }}
              style={{ fontSize: 13 }}
            />
          </Card>
        )}
      </div>
    </div>
  );
}
