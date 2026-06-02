// src/shared/components/StatusTag.tsx
const colorMap: Record<string, { bg: string; color: string }> = {
  ACTIVE: { bg: '#f0f5ff', color: '#597ef7' },
  SUCCESS: { bg: '#f0fdf4', color: '#16a34a' },
  COMPLETED: { bg: '#f0fdf4', color: '#16a34a' },
  PENDING: { bg: '#fffcf0', color: '#d97706' },
  RUNNING: { bg: '#f0f5ff', color: '#597ef7' },
  FAILED: { bg: '#fff0f0', color: '#eb5757' },
  ERROR: { bg: '#fff0f0', color: '#eb5757' },
  DRAFT: { bg: '#f5f5f5', color: '#666' },
  DISCONNECTED: { bg: '#f5f5f5', color: '#666' },
  UNKNOWN: { bg: '#f5f5f5', color: '#999' },
};

interface StatusTagProps {
  status: string;
}

export default function StatusTag({ status }: StatusTagProps) {
  const c = colorMap[status] ?? { bg: '#f5f5f5', color: '#666' };
  return (
    <span
      style={{
        display: 'inline-block',
        padding: '3px 10px',
        borderRadius: 6,
        fontSize: 11,
        fontWeight: 500,
        background: c.bg,
        color: c.color,
        lineHeight: 1.5,
      }}
    >
      {status}
    </span>
  );
}
