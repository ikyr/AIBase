import { create } from 'zustand';
import { listSkills, getSkillById, getSkillVersions, createSkill, updateSkill, deleteSkill, type SkillDef, type SkillVersion, type SkillCreateRequest } from '../../../shared/api/skill';

const levelLabel: Record<string, string> = { PROMPT: 'Prompt模板', FUNCTION: '函数', AGENT: '子Agent' };
const levelColor: Record<string, string> = { PROMPT: '#f0f5ff', FUNCTION: '#f0fdf4', AGENT: '#fffcf0' };

interface SkillState {
  skills: SkillDef[];
  loading: boolean;
  detail: SkillDef | null;
  versions: SkillVersion[];
  fetchList: () => Promise<void>;
  fetchDetail: (id: string) => Promise<void>;
  fetchVersions: (id: string) => Promise<void>;
  create: (data: SkillCreateRequest) => Promise<SkillDef | null>;
  update: (id: string, data: Partial<SkillCreateRequest>) => Promise<SkillDef | null>;
  remove: (id: string) => Promise<boolean>;
}

export const useSkillStore = create<SkillState>((set) => ({
  skills: [],
  loading: false,
  detail: null,
  versions: [],
  fetchList: async () => {
    set({ loading: true });
    const res = await listSkills();
    set({ skills: res.success ? (res.data ?? []) : [], loading: false });
  },
  fetchDetail: async (id: string) => {
    set({ loading: true });
    const res = await getSkillById(id);
    set({ detail: res.success ? (res.data ?? null) : null, loading: false });
  },
  fetchVersions: async (id: string) => {
    set({ loading: true });
    const res = await getSkillVersions(id);
    set({ versions: res.success ? (res.data ?? []) : [], loading: false });
  },
  create: async (data: SkillCreateRequest) => {
    set({ loading: true });
    const res = await createSkill(data);
    if (res.success) {
      set((s) => ({ skills: [...s.skills, res.data!], loading: false }));
      return res.data;
    }
    set({ loading: false });
    return null;
  },
  update: async (id: string, data: Partial<SkillCreateRequest>) => {
    set({ loading: true });
    const res = await updateSkill(id, data);
    if (res.success) {
      set((s) => ({
        skills: s.skills.map((sk) => (sk.id === id ? { ...sk, ...res.data } : sk)),
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
    const res = await deleteSkill(id);
    if (res.success) {
      set((s) => ({ skills: s.skills.filter((sk) => sk.id !== id), loading: false }));
      return true;
    }
    set({ loading: false });
    return false;
  },
}));
export { levelLabel, levelColor };
