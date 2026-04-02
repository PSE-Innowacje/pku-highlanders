import { Page, expect } from '@playwright/test';

export class ContractorTypesPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto('/admin/contractor-types');
    await this.page.waitForURL('http://localhost:5173/admin/contractor-types', { timeout: 15_000 });
    await this.page.waitForSelector('table tbody tr', { timeout: 10_000 });
  }

  async expectHeading() {
    await expect(this.page.getByRole('heading', { name: 'Typy kontrahentów' })).toBeVisible();
  }

  async openAddDialog() {
    await this.page.getByRole('button', { name: 'Dodaj' }).click();
    await expect(this.page.getByText('Dodaj typ kontrahenta')).toBeVisible();
  }

  async fillForm(symbol: string, name: string) {
    await this.page.fill('#symbol', symbol);
    await this.page.fill('#name', name);
  }

  async saveForm() {
    await this.page.getByRole('button', { name: 'Zapisz' }).click();
    await expect(this.page.getByText('Dodaj typ kontrahenta').or(this.page.getByText('Edytuj typ kontrahenta'))).not.toBeVisible({ timeout: 5_000 });
  }

  async cancelForm() {
    await this.page.getByRole('button', { name: 'Anuluj' }).click();
  }

  async expectRowWithSymbol(symbol: string) {
    await expect(this.page.locator('td', { hasText: symbol })).toBeVisible({ timeout: 5_000 });
  }

  async deleteRowWithSymbol(symbol: string) {
    const row = this.page.locator('tr', { has: this.page.locator(`td:text("${symbol}")`) });
    this.page.once('dialog', dialog => dialog.accept());
    await row.getByRole('button', { name: 'Usuń' }).click();
    await this.page.waitForTimeout(500);
  }

  async editRowWithSymbol(symbol: string) {
    const row = this.page.locator('tr', { has: this.page.locator(`td:text("${symbol}")`) });
    await row.getByRole('button', { name: 'Edytuj' }).click();
    await expect(this.page.getByText('Edytuj typ kontrahenta')).toBeVisible();
  }

  rows() {
    return this.page.locator('table tbody tr');
  }
}
