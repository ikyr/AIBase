// src/shared/components/PageHeader.tsx
import { Button } from 'antd';

interface PageHeaderProps {
  title: string;
  actionLabel?: string;
  onAction?: () => void;
  children?: React.ReactNode;
}

export default function PageHeader({ title, actionLabel, onAction, children }: PageHeaderProps) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 16,
        padding: '20px 20px 0',
      }}
    >
      <div>
        <h2 style={{ fontSize: 20, fontWeight: 700, margin: 0, letterSpacing: '-0.3px' }}>{title}</h2>
        {children}
      </div>
      {actionLabel && onAction && (
        <Button type="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
