import { Page, expect } from '@playwright/test';

export class DeclarationsPage {
  constructor(private readonly page: Page) {}

  async goto() {
    await this.page.goto('/declarations/pending');
    await this.page.waitForURL('**/declarations/pending', { timeout: 5_000 });
  }

  async generateDeclarations() {
    await this.page.getByRole('button', { name: 'Wygeneruj oświadczenia' }).click();
    // Wait for the list to refresh
    await this.page.waitForTimeout(1_500);
  }

  async expectDeclarationsVisible() {
    await expect(this.page.locator('table tbody tr').first()).toBeVisible({ timeout: 8_000 });
  }

  async expectEmptyMessage() {
    await expect(this.page.getByText(/Brak oświadczeń/)).toBeVisible({ timeout: 5_000 });
  }

  declarationRows() {
    return this.page.locator('table tbody tr');
  }

  fillButtonForRow(index: number) {
    return this.declarationRows().nth(index).getByRole('button', { name: 'Wypełnij' });
  }

  submitButtonForRow(index: number) {
    return this.declarationRows().nth(index).getByRole('button', { name: 'Wyślij' });
  }

  async openFillModal(rowIndex: number) {
    await this.fillButtonForRow(rowIndex).click();
    await expect(this.page.getByText('Wypełnij oświadczenie')).toBeVisible({ timeout: 5_000 });
  }

  async fillField(fieldCode: string, value: string) {
    const row = this.page.locator('tr', { has: this.page.locator(`code:text("${fieldCode}")`) });
    await row.locator('input').fill(value);
  }

  async saveDeclaration() {
    await this.page.getByRole('button', { name: 'Zapisz' }).click();
    // Modal should close
    await expect(this.page.getByText('Wypełnij oświadczenie')).not.toBeVisible({ timeout: 5_000 });
  }

  async cancelModal() {
    await this.page.getByRole('button', { name: 'Anuluj' }).click();
  }

  async expectStatusInRow(rowIndex: number, statusText: string) {
    await expect(this.declarationRows().nth(rowIndex).getByText(statusText)).toBeVisible();
  }
}
