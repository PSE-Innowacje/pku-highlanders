import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DeclarationTypesPage } from '../pages/DeclarationTypesPage';

test.describe('Administracja — typy oświadczeń', () => {
  test.beforeEach(async ({ page }) => {
    await new LoginPage(page).loginAs('admin@pse.pl', 'admin123');
  });

  test('strona wyświetla listę typów oświadczeń', async ({ page }) => {
    const declarationTypesPage = new DeclarationTypesPage(page);

    await declarationTypesPage.goto();
    await declarationTypesPage.expectHeading();
    await declarationTypesPage.expectRowCount(1);

    // Verify table columns are rendered in Polish
    const headers = declarationTypesPage.columnHeaders();
    await expect(headers.filter({ hasText: 'Kod' })).toBeVisible();
    await expect(headers.filter({ hasText: 'Nazwa' })).toBeVisible();
  });

  test('podgląd szczegółów typu oświadczenia pokazuje pola', async ({ page }) => {
    const declarationTypesPage = new DeclarationTypesPage(page);

    await declarationTypesPage.goto();
    await declarationTypesPage.previewFirstType();
    await declarationTypesPage.expectFieldTableVisible();

    // Detail page should have field list table
    await expect(page.locator('table')).toBeVisible();
  });

  test('nawigacja do szczegółów i powrót', async ({ page }) => {
    const declarationTypesPage = new DeclarationTypesPage(page);

    await declarationTypesPage.goto();
    await declarationTypesPage.previewFirstType();

    // Navigate back via sidebar
    await page.getByRole('button', { name: 'Typy oświadczeń', exact: true }).first().click();
    await page.waitForURL('http://localhost:5173/admin/declaration-types', { timeout: 10_000 });
    await declarationTypesPage.expectHeading();
  });
});
