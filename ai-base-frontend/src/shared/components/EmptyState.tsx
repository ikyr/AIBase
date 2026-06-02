// src/shared/components/EmptyState.tsx
import { Button } from 'antd';

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export default function EmptyState({ icon = '📭', title, description, actionLabel, onAction }: EmptyStateProps) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 60, color: '#999', gap: 8 }}>
      <span style={{ fontSize: 40, lineHeight: 1 }}>{icon}</span>
      <span style={{ fontSize: 14, fontWeight: 500, color: '#555' }}>{title}</span>
      {description && <span style={{ fontSize: 12 }}>{description}</span>}
      {actionLabel && onAction && (
        <Button type="primary" onClick={onAction} style={{ marginTop: 8 }}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
