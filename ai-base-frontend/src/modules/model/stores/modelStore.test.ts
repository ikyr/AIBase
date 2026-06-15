import { describe, it, expect, beforeEach, vi } from 'vitest';

// Mock the API module
vi.mock('../../../shared/api/model', () => ({
  listModels: vi.fn(),
  listRouteRules: vi.fn(),
  listCallLogs: vi.fn(),
  createModel: vi.fn(),
  updateModel: vi.fn(),
  deleteModel: vi.fn(),
  createRouteRule: vi.fn(),
  deleteRouteRule: vi.fn(),
}));

import { useModelStore } from './modelStore';
import { listModels, listRouteRules, createModel, createRouteRule } from '../../../shared/api/model';

describe('modelStore', () => {
  beforeEach(() => {
    useModelStore.setState({
      models: [],
      rules: [],
      logs: [],
      loading: false,
    });
    vi.clearAllMocks();
  });

  describe('fetchList', () => {
    it('sets models on successful fetch', async () => {
      const mockModels = [{ id: '1', name: 'GPT-4', provider: 'OPENAI', endpoint: '', apiKeyRef: '', maxTokens: 4096, capabilities: 'chat', priority: 0, status: 'ACTIVE', createdAt: '2024-01-01' }];
      vi.mocked(listModels).mockResolvedValue({ success: true, data: mockModels, error: null });

      await useModelStore.getState().fetchList();

      expect(useModelStore.getState().models).toEqual(mockModels);
      expect(useModelStore.getState().loading).toBe(false);
    });

    it('sets empty array on failed fetch', async () => {
      vi.mocked(listModels).mockResolvedValue({ success: false, data: null, error: 'Error' });

      await useModelStore.getState().fetchList();

      expect(useModelStore.getState().models).toEqual([]);
    });
  });

  describe('fetchRules', () => {
    it('sets rules on successful fetch', async () => {
      const mockRules = [{ id: 'r1', name: 'High Priority', modelId: '1', matchExpression: 'capability == chat', priority: 10, fallbackModelId: '2', status: 'ACTIVE' }];
      vi.mocked(listRouteRules).mockResolvedValue({ success: true, data: mockRules, error: null });

      await useModelStore.getState().fetchRules();

      expect(useModelStore.getState().rules).toEqual(mockRules);
    });
  });

  describe('create', () => {
    it('adds model to list on success', async () => {
      const newModel = { id: '2', name: 'Claude', provider: 'OPENAI', endpoint: '', apiKeyRef: '', maxTokens: 8192, capabilities: 'chat', priority: 0, status: 'ACTIVE', createdAt: '2024-01-01' };
      vi.mocked(createModel).mockResolvedValue({ success: true, data: newModel, error: null });

      const result = await useModelStore.getState().create({ name: 'Claude', provider: 'OPENAI' });

      expect(result).toEqual(newModel);
      expect(useModelStore.getState().models).toContainEqual(newModel);
    });

    it('returns null on failure', async () => {
      vi.mocked(createModel).mockResolvedValue({ success: false, data: null, error: 'Failed' });

      const result = await useModelStore.getState().create({ name: 'Claude' });

      expect(result).toBeNull();
    });
  });

  describe('createRule', () => {
    it('adds rule on success', async () => {
      const newRule = { id: 'nr1', name: 'Test Rule', modelId: '1', matchExpression: '*', priority: 0, fallbackModelId: '', status: 'ACTIVE' };
      vi.mocked(createRouteRule).mockResolvedValue({ success: true, data: newRule, error: null });

      const result = await useModelStore.getState().createRule({ name: 'Test Rule', modelId: '1', matchExpression: '*' });

      expect(result).toEqual(newRule);
      expect(useModelStore.getState().rules).toContainEqual(newRule);
    });

    it('returns null on failure', async () => {
      vi.mocked(createRouteRule).mockResolvedValue({ success: false, data: null, error: 'Error' });

      const result = await useModelStore.getState().createRule({ name: 'X', modelId: '1', matchExpression: '*' });

      expect(result).toBeNull();
    });
  });
});
