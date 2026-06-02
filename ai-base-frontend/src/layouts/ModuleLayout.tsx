// src/layouts/ModuleLayout.tsx
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

interface SideMenuItem {
  key: string;
  label: string;
  path: string;
}

interface ModuleLayoutProps {
  title: string;
  menu: SideMenuItem[];
  children?: React.ReactNode;
}

export default function ModuleLayout({ title, menu, children }: ModuleLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      {/* Sidebar */}
      <aside
        style={{
          width: 200,
          background: '#fafafa',
          borderRight: '1px solid #f0f0f0',
          padding: '12px 8px',
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
          flexShrink: 0,
        }}
      >
        <div
          style={{
            fontSize: 11,
            color: '#999',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            fontWeight: 600,
            padding: '8px 10px 4px',
          }}
        >
          {title}
        </div>
        {menu.map((item) => (
          <button
            key={item.key}
            onClick={() => navigate(item.path)}
            style={{
              display: 'block',
              width: '100%',
              textAlign: 'left',
              padding: '7px 10px',
              fontSize: 13,
              color: location.pathname === item.path ? '#597ef7' : '#555',
              fontWeight: location.pathname === item.path ? 500 : 400,
              borderRadius: 8,
              border: 'none',
              background:
                location.pathname === item.path ? '#fff' : 'transparent',
              boxShadow:
                location.pathname === item.path
                  ? '0 1px 3px rgba(0,0,0,0.04)'
                  : 'none',
              cursor: 'pointer',
              transition: 'all 0.15s',
              fontFamily: 'inherit',
            }}
          >
            {item.label}
          </button>
        ))}
      </aside>

      {/* Content */}
      <div style={{ flex: 1, overflow: 'auto', background: '#fff' }}>
        {children ?? <Outlet />}
      </div>
    </div>
  );
}
