import { useEffect, useState, useCallback, useRef, type DragEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  addEdge,
  type Node,
  type Edge,
  type Connection,
  type NodeTypes,
  BackgroundVariant,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Spin, Button, message } from 'antd';
import { SaveOutlined } from '@ant-design/icons';
import { useWorkflowStore } from '../stores/workflowStore';
import { updateWorkflow } from '../../../shared/api/workflow';
import EmptyState from '../../../shared/components/EmptyState';
import WorkflowNode from '../components/WorkflowNode';
import NodePalette from '../components/NodePalette';
import NodeConfigPanel from '../components/NodeConfigPanel';
import type { WorkflowNodeData } from '../components/WorkflowNode';

const nodeTypes: NodeTypes = { workflow: WorkflowNode };

let nodeIdCounter = 0;
function nextNodeId() {
  nodeIdCounter += 1;
  return `node_${Date.now().toString(36)}_${nodeIdCounter}`;
}

function dagNodeToRfNode(dn: { id: string; name: string; type: string; refId?: string; refName?: string }): Node {
  return {
    id: dn.id,
    type: 'workflow',
    position: { x: 100 + Math.random() * 400, y: 80 + Math.random() * 300 },
    data: {
      label: dn.name || dn.id,
      nodeType: dn.type,
      refId: dn.refId,
      config: {},
    },
  };
}

function dagEdgeToRfEdge(de: { from: string; to: string; label?: string }): Edge {
  return {
    id: `${de.from}-${de.to}`,
    source: de.from,
    target: de.to,
    label: de.label,
    animated: true,
  };
}

export default function WorkflowEditorPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { detail, dag, loading, fetchDetail } = useWorkflowStore();
  const reactFlowWrapper = useRef<HTMLDivElement>(null);

  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [configOpen, setConfigOpen] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (id) fetchDetail(id);
  }, [id, fetchDetail]);

  useEffect(() => {
    if (dag) {
      nodeIdCounter = dag.nodes.length;
      setNodes(dag.nodes.map(dagNodeToRfNode));
      setEdges(dag.edges.map(dagEdgeToRfEdge));
    }
  }, [dag, setNodes, setEdges]);

  const selectedNode = nodes.find((n) => n.id === selectedNodeId) ?? null;

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge({ ...params, animated: true }, eds)),
    [setEdges],
  );

  const onNodeClick = useCallback((_: unknown, node: Node) => {
    setSelectedNodeId(node.id);
    setConfigOpen(true);
  }, []);

  const onDragOver = useCallback((e: DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  }, []);

  const onDrop = useCallback(
    (e: DragEvent) => {
      e.preventDefault();
      const nodeType = e.dataTransfer.getData('application/reactflow-type');
      if (!nodeType || !reactFlowWrapper.current) return;

      const bounds = reactFlowWrapper.current.getBoundingClientRect();
      const position = { x: e.clientX - bounds.left - 50, y: e.clientY - bounds.top - 25 };

      const labelMap: Record<string, string> = {
        START: '开始', END: '结束', AGENT: 'Agent', SKILL: 'Skill',
        TOOL: 'Tool', KNOWLEDGE: '知识检索', CONDITION: '条件判断',
        PARALLEL: '并行执行', LLM_CALL: 'LLM 调用', CODE: '脚本执行', WAIT: '等待审批',
        QUESTION_CLASSIFIER: '问题分类', VARIABLE_ASSIGNER: '变量赋值', HTTP_REQUEST: 'HTTP 请求',
      };

      const newNode: Node = {
        id: nextNodeId(),
        type: 'workflow',
        position,
        data: {
          label: labelMap[nodeType] ?? nodeType,
          nodeType,
          refId: '',
          config: {},
        },
      };
      setNodes((nds) => [...nds, newNode]);
    },
    [setNodes],
  );

  const handleSaveConfig = (nodeId: string, data: WorkflowNodeData) => {
    setNodes((nds) =>
      nds.map((n) => (n.id === nodeId ? { ...n, data: { ...n.data, ...data } } : n)),
    );
  };

  const handleDeleteNode = (nodeId: string) => {
    setNodes((nds) => nds.filter((n) => n.id !== nodeId));
    setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));
  };

  const handleSave = async () => {
    if (!id || !detail) return;
    setSaving(true);
    const dagPayload = {
      nodes: nodes.map((n) => ({
        id: n.id,
        name: (n.data as unknown as WorkflowNodeData).label,
        type: (n.data as unknown as WorkflowNodeData).nodeType,
        refId: (n.data as unknown as WorkflowNodeData).refId || '',
        refName: '',
      })),
      edges: edges.map((e) => ({
        from: e.source,
        to: e.target,
        label: e.label?.toString() ?? '',
      })),
    };

    try {
      await updateWorkflow(id, { dag: JSON.stringify(dagPayload) });
      message.success('工作流保存成功');
    } catch {
      message.error('保存失败');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Spin style={{ display: 'block', margin: '60px auto' }} />;
  if (!detail) return <EmptyState icon="⚙️" title="工作流不存在" />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 64px)' }}>
      {/* Header */}
      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '12px 20px', borderBottom: '1px solid #f0f0f0', background: '#fff',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Button type="link" onClick={() => navigate('/workflow')} style={{ padding: 0 }}>
            ← 返回
          </Button>
          <span style={{ fontSize: 16, fontWeight: 700 }}>{detail.name}</span>
          {detail.version > 0 && (
            <span style={{ fontSize: 12, color: '#999' }}>v{detail.version}</span>
          )}
        </div>
        <Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={handleSave}>
          保存工作流
        </Button>
      </div>

      {/* Main content: palette + canvas */}
      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <NodePalette />

        <div ref={reactFlowWrapper} style={{ flex: 1, height: '100%' }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={onNodeClick}
            onDragOver={onDragOver}
            onDrop={onDrop}
            nodeTypes={nodeTypes}
            fitView
            deleteKeyCode={['Backspace', 'Delete']}
            style={{ background: '#fafbfc' }}
          >
            <Background variant={BackgroundVariant.Dots} gap={20} size={1} color="#e8e8e8" />
            <Controls />
            <MiniMap
              nodeColor={(n) => {
                const d = n.data as unknown as WorkflowNodeData;
                const colors: Record<string, string> = {
                  START: '#52c41a', END: '#ff4d4f', AGENT: '#1677ff',
                  SKILL: '#722ed1', TOOL: '#fa8c16', KNOWLEDGE: '#13c2c2',
                  CONDITION: '#fadb14', PARALLEL: '#8c8c8c', LLM_CALL: '#2f54eb',
                  CODE: '#434343', WAIT: '#d48806',
                  QUESTION_CLASSIFIER: '#eb2f96', VARIABLE_ASSIGNER: '#faad14', HTTP_REQUEST: '#fa541c',
                };
                return colors[d.nodeType] ?? '#d9d9d9';
              }}
            />
          </ReactFlow>
        </div>
      </div>

      <NodeConfigPanel
        open={configOpen}
        data={(selectedNode?.data as unknown as WorkflowNodeData) ?? null}
        nodeId={selectedNodeId ?? ''}
        onClose={() => setConfigOpen(false)}
        onSave={handleSaveConfig}
        onDelete={handleDeleteNode}
      />
    </div>
  );
}
