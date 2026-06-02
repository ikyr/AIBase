// src/shared/stores/userStore.ts
import { create } from 'zustand';

interface UserState {
  userId: string;
  deptId: string;
  userName: string;
  setUser: (userId: string, deptId: string, userName: string) => void;
}

export const useUserStore = create<UserState>((set) => ({
  userId: localStorage.getItem('userId') || 'dev-user',
  deptId: localStorage.getItem('deptId') || 'dev-dept',
  userName: localStorage.getItem('userName') || '开发者',
  setUser: (userId: string, deptId: string, userName: string) => {
    localStorage.setItem('userId', userId);
    localStorage.setItem('deptId', deptId);
    localStorage.setItem('userName', userName);
    set({ userId, deptId, userName });
  },
}));
