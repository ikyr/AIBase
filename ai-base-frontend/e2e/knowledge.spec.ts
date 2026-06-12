import { test, expect } from '@playwright/test';

test.describe('Knowledge — 知识库管理', () => {
  test('should navigate to knowledge page from top nav', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: '📚 知识库' }).click();
    await expect(page).toHaveURL(/\/knowledge/);
    await expect(page.getByText('知识库')).toBeVisible();
  });

  test('should show knowledge base list or empty state', async ({ page }) => {
    await page.goto('/knowledge');
    await page.waitForTimeout(1000);

    const hasCards = await page.locator('.ant-card').count() > 0;
    const hasEmpty = await page.getByText('暂无知识库').isVisible().catch(() => false);
    expect(hasCards || hasEmpty).toBeTruthy();
  });

  test('should open create KB modal and validate form', async ({ page }) => {
    await page.goto('/knowledge');
    await page.getByRole('button', { name: '新建知识库' }).click();
    await expect(page.getByText('创建知识库')).toBeVisible();

    // Submit empty form
    await page.getByRole('button', { name: '创建' }).click();
    await expect(page.getByText('请输入知识库名称')).toBeVisible();
  });

  test('should create a new knowledge base', async ({ page }) => {
    await page.goto('/knowledge');
    await page.waitForTimeout(500);

    await page.getByRole('button', { name: '新建知识库' }).click();
    await page.getByLabel('名称').fill('E2E Test KB');
    await page.getByLabel('描述').fill('Created by Playwright E2E test');
    await page.getByRole('button', { name: '创建' }).click();

    await page.waitForTimeout(500);
    await expect(page.getByText('E2E Test KB')).toBeVisible({ timeout: 5000 });
  });
});
