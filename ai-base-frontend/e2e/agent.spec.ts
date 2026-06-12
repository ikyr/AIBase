import { test, expect } from '@playwright/test';

test.describe('Agent — Agent 管理', () => {
  test('should navigate to agent page from top nav', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: '🤖 Agent' }).click();
    await expect(page).toHaveURL(/\/agent/);
    await expect(page.getByText('Agent 管理')).toBeVisible();
  });

  test('should show agent list or empty state', async ({ page }) => {
    await page.goto('/agent');
    await page.waitForTimeout(1000);

    const hasCards = await page.locator('.ant-card').count() > 0;
    const hasEmpty = await page.getByText('暂无 Agent').isVisible().catch(() => false);
    expect(hasCards || hasEmpty).toBeTruthy();
  });

  test('should show top nav with all 8 items', async ({ page }) => {
    await page.goto('/');

    const navItems = ['💬 对话', '⚙️ 工作流', '📚 知识库', '🤖 Agent', '🛠 Skill', '🔌 MCP', '🧠 模型', '📊 评估'];
    for (const item of navItems) {
      await expect(page.getByRole('button', { name: item })).toBeVisible();
    }
  });

  test('should navigate between modules without errors', async ({ page }) => {
    await page.goto('/');

    const modules = [
      { name: '📚 知识库', url: /\/knowledge/ },
      { name: '⚙️ 工作流', url: /\/workflow/ },
      { name: '🛠 Skill', url: /\/skill/ },
      { name: '🔌 MCP', url: /\/mcp/ },
      { name: '🧠 模型', url: /\/model/ },
      { name: '📊 评估', url: /\/eval/ },
    ];

    for (const mod of modules) {
      await page.getByRole('button', { name: mod.name }).click();
      await expect(page).toHaveURL(mod.url);
      await page.waitForTimeout(300);
      await expect(page.locator('body')).toBeVisible();
    }
  });
});
