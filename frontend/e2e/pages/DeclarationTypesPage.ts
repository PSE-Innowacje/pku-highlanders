import { Page, expect } from '@playwright/test';

export class DeclarationTypesPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto('/admin/declaration-types');
    await this.page.waitForURL('http://localhost:5173/admin/declaration-types', { timeout: 15_000 });
  }

  async expectHeading() {
    await expect(this.page.getByRole('heading', { name: 'Typy oświadczeń' })).toBeVisible();
  }

  async expectRowCount(minCount: number) {
    await expect(this.page.locator('table tbody tr').nth(minCount - 1)).toBeVisible({ timeout: 5_000 });
  }

  async previewFirstType() {
    await this.page.locator('table tbody tr').first().getByRole('button', { name: 'Podgląd' }).click();
    await this.page.waitForURL(/\/admin\/declaration-types\/.+/, { timeout: 5_000 });
  }

  async expectFieldTableVisible() {
    await expect(this.page.locator('table')).toBeVisible({ timeout: 5_000 });
  }

  columnHeaders() {
    return this.page.locator('table thead th');
  }
}
