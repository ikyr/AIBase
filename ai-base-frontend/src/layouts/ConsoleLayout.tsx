// src/layouts/ConsoleLayout.tsx
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { UserOutlined } from '@ant-design/icons';
import { useUserStore } from '../shared/stores/userStore';
import './ConsoleLayout.css';

interface NavItem {
  key: string;
  label: string;
  path: string;
  icon: string;
  group: 'business' | 'admin';
}

const navItems: NavItem[] = [
  { key: 'chat', label: '对话', path: '/', icon: '💬', group: 'business' },
  { key: 'workflow', label: '工作流', path: '/workflow', icon: '⚙️', group: 'business' },
  { key: 'knowledge', label: '知识库', path: '/knowledge', icon: '📚', group: 'business' },
  { key: 'agent', label: 'Agent', path: '/agent', icon: '🤖', group: 'admin' },
  { key: 'skill', label: 'Skill', path: '/skill', icon: '🛠', group: 'admin' },
  { key: 'mcp', label: 'MCP', path: '/mcp', icon: '🔌', group: 'admin' },
  { key: 'model', label: '模型', path: '/model', icon: '🧠', group: 'admin' },
  { key: 'eval', label: '评估', path: '/eval', icon: '📊', group: 'admin' },
  { key: 'platform', label: '平台', path: '/platform/prompts', icon: '🏛️', group: 'admin' },
];

export default function ConsoleLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const userName = useUserStore((s) => s.userName);

  const isActive = (item: NavItem) => {
    if (item.path === '/') return location.pathname === '/';
    return location.pathname.startsWith(item.path);
  };

  const businessItems = navItems.filter((i) => i.group === 'business');
  const adminItems = navItems.filter((i) => i.group === 'admin');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      {/* Top navigation bar */}
      <header className="console-topbar">
        <div className="console-topbar-left">
          <span
            className="console-logo"
            onClick={() => navigate('/')}
            style={{ cursor: 'pointer' }}
          >
            AI<span className="console-logo-highlight">Base</span>
          </span>
          <nav className="console-nav">
            {businessItems.map((item) => (
              <button
                key={item.key}
                className={`console-nav-item${isActive(item) ? ' active' : ''}`}
                onClick={() => navigate(item.path)}
              >
                <span className="console-nav-icon">{item.icon}</span>
                {item.label}
              </button>
            ))}
            <span className="console-nav-divider" />
            {adminItems.map((item) => (
              <button
                key={item.key}
                className={`console-nav-item${isActive(item) ? ' active' : ''}`}
                onClick={() => navigate(item.path)}
              >
                <span className="console-nav-icon">{item.icon}</span>
                {item.label}
              </button>
            ))}
          </nav>
        </div>
        <div className="console-user-chip">
          <UserOutlined style={{ fontSize: 12 }} />
          <span>{userName}</span>
        </div>
      </header>

      {/* Page content */}
      <main style={{ flex: 1, overflow: 'hidden' }}>
        <Outlet />
      </main>
    </div>
  );
}
