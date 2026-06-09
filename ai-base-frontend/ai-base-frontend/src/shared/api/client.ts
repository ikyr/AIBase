const BASE_URLS: Record<string, string> = {
  chat: 'http://localhost:8080/api',
  workflow: 'http://localhost:8082/api',
  knowledge: 'http://localhost:8083/api',
  agent: 'http://localhost:8084/api',
  skill: 'http://localhost:8085/api',
  mcp: 'http://localhost:8086/api',
  model: 'http://localhost:8087/api',
  eval: 'http://localhost:8088/api',
  platform: 'http://localhost:8089/api',
};

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  meta?: { total: number; page: number; limit: number };
}

async function request<T>(service: keyof typeof BASE_URLS, path: string, options?: RequestInit): Promise<ApiResponse<T>> {
  const url = `${BASE_URLS[service]}${path}`;
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  if (!res.ok) {
    return { success: false, error: `HTTP ${res.status}: ${res.statusText}` };
  }
  return res.json();
}

export function get<T>(service: keyof typeof BASE_URLS, path: string): Promise<ApiResponse<T>> {
  return request<T>(service, path);
}

export function post<T>(service: keyof typeof BASE_URLS, path: string, body: unknown): Promise<ApiResponse<T>> {
  return request<T>(service, path, { method: 'POST', body: JSON.stringify(body) });
}

export function put<T>(service: keyof typeof BASE_URLS, path: string, body: unknown): Promise<ApiResponse<T>> {
  return request<T>(service, path, { method: 'PUT', body: JSON.stringify(body) });
}
