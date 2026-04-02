import { Page, expect } from '@playwright/test';

export class DashboardPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto('/');
    await this.page.waitForURL('http://localhost:5173/', { timeout: 5_000 });
  }

  async expectUsername(username: string) {
    await expect(this.page.getByText(username).first()).toBeVisible();
  }

  async expectRole(role: string) {
    await expect(this.page.getByText(role)).toBeVisible();
  }

  sidebarLink(name: string) {
    return this.page.getByRole('button', { name, exact: true }).first();
  }
}
