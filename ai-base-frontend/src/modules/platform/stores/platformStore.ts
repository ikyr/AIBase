import { create } from 'zustand';
import { listPrompts, listApprovals, approveApproval, rejectApproval, type PromptVersion, type ApprovalRecord } from '../../../shared/api/platform';

const typeLabel: Record<string, string> = { PUBLISH: '发布', DEPLOY: '部署', EXECUTE: '执行' };
const typeColor: Record<string, string> = { PUBLISH: '#f0f5ff', DEPLOY: '#f0fdf4', EXECUTE: '#fffcf0' };
const statusLabel: Record<string, string> = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' };
const statusColor: Record<string, string> = { PENDING: '#d97706', APPROVED: '#16a34a', REJECTED: '#eb5757' };

interface PlatformState {
  prompts: PromptVersion[];
  approvals: ApprovalRecord[];
  loading: boolean;
  error: string | null;
  fetchPrompts: () => Promise<void>;
  fetchApprovals: () => Promise<void>;
  approve: (id: string) => Promise<void>;
  reject: (id: string) => Promise<void>;
}

export const usePlatformStore = create<PlatformState>((set) => ({
  prompts: [],
  approvals: [],
  loading: false,
  error: null,
  fetchPrompts: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listPrompts();
      if (!res.success) throw new Error(res.error || 'Failed to fetch prompts');
      set({ prompts: res.data ?? [], loading: false });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
  fetchApprovals: async () => {
    set({ loading: true, error: null });
    try {
      const res = await listApprovals();
      if (!res.success) throw new Error(res.error || 'Failed to fetch approvals');
      set({ approvals: res.data ?? [], loading: false });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
  approve: async (id: string) => {
    set({ loading: true, error: null });
    try {
      const res = await approveApproval(id);
      if (!res.success || !res.data) throw new Error(res.error || 'Failed to approve');
      set((s) => ({
        approvals: s.approvals.map((a) => (a.id === id ? res.data! : a)),
        loading: false,
      }));
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
  reject: async (id: string) => {
    set({ loading: true, error: null });
    try {
      const res = await rejectApproval(id);
      if (!res.success || !res.data) throw new Error(res.error || 'Failed to reject');
      set((s) => ({
        approvals: s.approvals.map((a) => (a.id === id ? res.data! : a)),
        loading: false,
      }));
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Unknown error';
      set({ error: message, loading: false });
    }
  },
}));
export { typeLabel, typeColor, statusLabel, statusColor };
