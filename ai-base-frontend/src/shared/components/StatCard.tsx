// src/shared/components/StatCard.tsx
interface StatCardProps {
  value: number | string;
  label: string;
}

export default function StatCard({ value, label }: StatCardProps) {
  return (
    <div
      style={{
        background: '#fafafa',
        borderRadius: 12,
        padding: '14px 16px',
        minWidth: 120,
        flex: 1,
      }}
    >
      <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.5px', lineHeight: 1.2 }}>
        {value}
      </div>
      <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{label}</div>
    </div>
  );
}
