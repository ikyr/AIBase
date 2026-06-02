// src/shared/api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — inject user context headers
client.interceptors.request.use((config) => {
  const userId = localStorage.getItem('userId') || 'dev-user';
  const deptId = localStorage.getItem('deptId') || 'dev-dept';
  config.headers['X-User-Id'] = userId;
  config.headers['X-Dept-Id'] = deptId;
  return config;
});

// Response interceptor — unwrap ApiResponse envelope
client.interceptors.response.use(
  (response) => response.data as any,
  (error) => {
    const message = error.response?.data?.error || error.message || 'Network error';
    console.error(`[API] ${error.config?.url}: ${message}`);
    return Promise.reject(error);
  }
);

export default client;
