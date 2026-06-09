import { create } from 'zustand';
import { listWorkflows, getWorkflowById, getWorkflowDag, listWorkflowInstances, createWorkflow, updateWorkflow, deleteWorkflow, type WfDefinition, type WfInstance, type DagView, type WfCreateRequest } from '../../../shared/api/workflow';

const stepTypeLabel: Record<string, string> = { AGENT: 'Agent', SKILL: 'Skill', CONDITION: '条件', START: '开始', END: '结束', TOOL: 'Tool', KNOWLEDGE: '知识检索', PARALLEL: '并行', LLM_CALL: 'LLM调用', CODE: '脚本', WAIT: '等待' };
const stepTypeColor: Record<string, string> = { AGENT: '#f0f5ff', SKILL: '#f0fdf4', CONDITION: '#fffcf0', START: '#f5f5f5', END: '#f5f5f5', TOOL: '#fff7e6', KNOWLEDGE: '#e6fffb', PARALLEL: '#f5f5f5', LLM_CALL: '#f0f5ff', CODE: '#f5f5f5', WAIT: '#fff7e6' };

interface WorkflowState {
  workflows: WfDefinition[];
  loading: boolean;
  detail: WfDefinition | null;
  dag: DagView | null;
  instances: WfInstance[];
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  fetchInstances: () => Promise<void>;
  create: (data: WfCreateRequest) => Promise<WfDefinition | null>;
  update: (id: string, data: Partial<WfCreateRequest>) => Promise<WfDefinition | null>;
  remove: (id: string) => Promise<boolean>;
}

export const useWorkflowStore = create<WorkflowState>((set) => ({
  workflows: [],
  loading: false,
  detail: null,
  dag: null,
  instances: [],
  fetchList: async () => {
    set({ loading: true });
    const res = await listWorkflows();
    set({ workflows: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchDetail: async (id: string) => {
    set({ loading: true });
    const [defRes, dagRes] = await Promise.all([getWorkflowById(id), getWorkflowDag(id)]);
    set({
      detail: defRes.success ? (defRes.data ?? null) : null,
      dag: dagRes.success ? (dagRes.data ?? null) : null,
      loading: false,
    });
  },
  fetchInstances: async () => {
    set({ loading: true });
    const res = await listWorkflowInstances();
    set({ instances: res.success ? (res.data ?? []) : [], loading: false });
  },
  create: async (data: WfCreateRequest) => {
    set({ loading: true });
    const res = await createWorkflow(data);
    if (res.success) {
      set((s) => ({ workflows: [...s.workflows, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  update: async (id: string, data: Partial<WfCreateRequest>) => {
    set({ loading: true });
    const res = await updateWorkflow(id, data);
    if (res.success) {
      set((s) => ({
        workflows: s.workflows.map((w) => (w.id === id ? { ...w, ...res.data } : w)),
        detail: s.detail?.id === id ? { ...s.detail, ...res.data } : s.detail,
        loading: false,
      }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  remove: async (id: string) => {
    set({ loading: true });
    const res = await deleteWorkflow(id);
    if (res.success) {
      set((s) => ({ workflows: s.workflows.filter((w) => w.id !== id), loading: false }));
      return true;
    }
    set({ loading: false });
    return false;
  },
}));
export { stepTypeLabel, stepTypeColor };
