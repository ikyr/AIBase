// src/App.tsx
import { RouterProvider } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import { themeTokens } from './shared/theme/tokens';
import { router } from './router';

export default function App() {
  return (
    <ConfigProvider theme={themeTokens}>
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}
