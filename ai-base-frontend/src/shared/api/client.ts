import axios from 'axios';

const client = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

client.interceptors.request.use((config) => {
  const userId = localStorage.getItem('userId') || 'dev-user';
  const deptId = localStorage.getItem('deptId') || 'dev-dept';
  const apiKey = localStorage.getItem('apiKey') || 'aibase-dev-key-2024';
  config.headers['X-User-Id'] = userId;
  config.headers['X-Dept-Id'] = deptId;
  config.headers['X-Api-Key'] = apiKey;
  return config;
});

client.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.error || error.message || 'Network error';
    console.error(`[API] ${error.config?.url}: ${message}`);
    return Promise.reject(error);
  }
);

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: string | null;
}

export async function get<T>(_module: string, path: string): Promise<ApiResponse<T>> {
  return client.get(path) as Promise<ApiResponse<T>>;
}

export async function post<T>(_module: string, path: string, data?: unknown): Promise<ApiResponse<T>> {
  return client.post(path, data) as Promise<ApiResponse<T>>;
}

export async function del<T>(_module: string, path: string): Promise<ApiResponse<T>> {
  return client.delete(path) as Promise<ApiResponse<T>>;
}

export default client;
