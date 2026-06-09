import { create } from 'zustand';
import { listPrompts, listApprovals, type PromptVersion, type ApprovalRecord } from '../../../shared/api/platform';

const typeLabel: Record<string, string> = { PUBLISH: '发布', DEPLOY: '部署', EXECUTE: '执行' };
const typeColor: Record<string, string> = { PUBLISH: '#f0f5ff', DEPLOY: '#f0fdf4', EXECUTE: '#fffcf0' };
const statusLabel: Record<string, string> = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' };
const statusColor: Record<string, string> = { PENDING: '#d97706', APPROVED: '#16a34a', REJECTED: '#eb5757' };

interface PlatformState {
  prompts: PromptVersion[];
  approvals: ApprovalRecord[];
  loading: boolean;
  fetchPrompts: () => Promise<void>;
  fetchApprovals: () => Promise<void>;
}

export const usePlatformStore = create<PlatformState>((set) => ({
  prompts: [],
  approvals: [],
  loading: false,
  fetchPrompts: async () => {
    set({ loading: true });
    const res = await listPrompts();
    set({ prompts: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchApprovals: async () => {
    set({ loading: true });
    const res = await listApprovals();
    set({ approvals: res.success ? (res.data ?? []) : [], loading: false });
  },
}));
export { typeLabel, typeColor, statusLabel, statusColor };
