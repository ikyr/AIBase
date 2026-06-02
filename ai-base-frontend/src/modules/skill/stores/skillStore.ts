// src/modules/skill/stores/skillStore.ts
import { create } from 'zustand';

interface SkillDef {
  id: string;
  name: string;
  description: string;
  skillLevel: 'PROMPT' | 'FUNCTION' | 'AGENT';
  status: string;
}

const mockSkills: SkillDef[] = [
  { id: '1', name: 'proposal-section-writer', description: '申报书各章节写作模板', skillLevel: 'PROMPT', status: 'ACTIVE' },
  { id: '2', name: 'proposal-formatter', description: '格式校验（字号/行距/页边距）', skillLevel: 'FUNCTION', status: 'ACTIVE' },
  { id: '3', name: 'budget-table-generator', description: '经费预算表自动计算与生成', skillLevel: 'FUNCTION', status: 'ACTIVE' },
  { id: '4', name: 'proposal-reviewer', description: '模拟评审专家审读草稿', skillLevel: 'AGENT', status: 'DRAFT' },
];

const levelLabel: Record<string, string> = { PROMPT: 'Prompt模板', FUNCTION: '函数', AGENT: '子Agent' };
const levelColor: Record<string, string> = { PROMPT: '#f0f5ff', FUNCTION: '#f0fdf4', AGENT: '#fffcf0' };

interface SkillState {
  skills: SkillDef[];
  loading: boolean;
  fetchList: () => Promise<void>;
}

export const useSkillStore = create<SkillState>((set) => ({
  skills: [],
  loading: false,
  fetchList: async () => {
    set({ loading: true });
    await new Promise((r) => setTimeout(r, 300));
    set({ skills: mockSkills, loading: false });
  },
}));
export { levelLabel, levelColor };
