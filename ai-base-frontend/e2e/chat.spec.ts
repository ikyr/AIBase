import { test, expect } from '@playwright/test';

test.describe('Chat Page — Agent 对话首页', () => {
  test('should display chat page with empty state', async ({ page }) => {
    await page.goto('/');

    // Should show the AIBase logo and navigation
    await expect(page.locator('.console-logo')).toBeVisible();
    await expect(page.locator('.console-nav')).toBeVisible();

    // Should show empty state with CTA
    await expect(page.getByText('开始一段新对话')).toBeVisible();
    await expect(page.getByRole('button', { name: '新建会话' })).toBeVisible();
  });

  test('should create new session and send message', async ({ page }) => {
    await page.goto('/');

    // Click "新建会话"
    await page.getByRole('button', { name: '新建会话' }).click();

    // Should show chat input
    const input = page.locator('textarea[placeholder*="输入消息"]');
    await expect(input).toBeVisible();

    // Type and send
    await input.fill('帮我写一个项目申报书提纲');
    await page.keyboard.press('Enter');

    // User message should appear
    await expect(page.getByText('帮我写一个项目申报书提纲')).toBeVisible();

    // AI reply should appear within 15 seconds
    await page.waitForTimeout(2000);
    const aiBubbles = page.locator('div[style*="rgb(245, 245, 245)"]');
    await expect(aiBubbles.first()).toBeVisible({ timeout: 15000 });
  });

  test('should show session in sidebar after chatting', async ({ page }) => {
    await page.goto('/');

    await page.getByRole('button', { name: '新建会话' }).click();
    const input = page.locator('textarea[placeholder*="输入消息"]');
    await input.fill('测试消息');
    await page.keyboard.press('Enter');

    // Session should appear in sidebar showing truncated message
    await page.waitForTimeout(500);
    const sidebar = page.locator('div').filter({ hasText: '测试消息' }).first();
    await expect(sidebar).toBeVisible({ timeout: 5000 });
  });
});
