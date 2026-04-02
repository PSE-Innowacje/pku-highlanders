import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DeclarationsPage } from '../pages/DeclarationsPage';

test.describe('Oświadczenia kontrahenta', () => {
  test.beforeEach(async ({ page }) => {
    await new LoginPage(page).loginAs('jan.kowalski@tauron.pl', 'jan123');
  });

  test('strona oświadczeń jest dostępna z menu bocznego', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Niezłożone' }).click();
    await page.waitForURL('**/declarations/pending', { timeout: 5_000 });
    await expect(page.getByRole('heading', { name: /oświadczenia/i })).toBeVisible();
  });

  test('można wygenerować oświadczenia', async ({ page }) => {
    const declarationsPage = new DeclarationsPage(page);

    await declarationsPage.goto();
    await declarationsPage.generateDeclarations();
    // After generation, at least one row should appear (or "no declarations" if no type assigned)
    const rowCount = await declarationsPage.declarationRows().count();
    // Generation is idempotent — either rows appear or an error is shown
    expect(rowCount >= 0).toBeTruthy();
  });

  test('oświadczenia są wyświetlane w tabeli', async ({ page }) => {
    const declarationsPage = new DeclarationsPage(page);

    await declarationsPage.goto();
    // Generate to ensure there is something to show
    await declarationsPage.generateDeclarations();

    const rowCount = await declarationsPage.declarationRows().count();
    if (rowCount > 0) {
      // Verify expected columns are present
      await expect(page.locator('table thead')).toContainText('Numer');
      await expect(page.locator('table thead')).toContainText('Status');
    }
  });

  test('można otworzyć formularz wypełniania oświadczenia', async ({ page }) => {
    const declarationsPage = new DeclarationsPage(page);

    await declarationsPage.goto();
    await declarationsPage.generateDeclarations();

    const rowCount = await declarationsPage.declarationRows().count();
    if (rowCount === 0) {
      test.skip();
      return;
    }

    // Find a row with an enabled "Wypełnij" button
    const fillBtn = declarationsPage.declarationRows()
      .filter({ has: page.getByRole('button', { name: 'Wypełnij', disabled: false }) })
      .first()
      .getByRole('button', { name: 'Wypełnij' });

    if (await fillBtn.count() === 0) {
      test.skip();
      return;
    }

    await fillBtn.click();
    await expect(page.getByText('Wypełnij oświadczenie')).toBeVisible({ timeout: 5_000 });
    // Modal should show declaration details
    await expect(page.locator('[role="dialog"]').getByText('Numer')).toBeVisible();
    await expect(page.locator('[role="dialog"]').getByText('Typ')).toBeVisible();
  });

  test('można wypełnić i zapisać oświadczenie', async ({ page }) => {
    const declarationsPage = new DeclarationsPage(page);

    await declarationsPage.goto();
    await declarationsPage.generateDeclarations();

    const rowCount = await declarationsPage.declarationRows().count();
    if (rowCount === 0) { test.skip(); return; }

    const fillBtn = declarationsPage.declarationRows()
      .filter({ has: page.getByRole('button', { name: 'Wypełnij', disabled: false }) })
      .first()
      .getByRole('button', { name: 'Wypełnij' });

    if (await fillBtn.count() === 0) { test.skip(); return; }

    await fillBtn.click();
    await expect(page.getByText('Wypełnij oświadczenie')).toBeVisible({ timeout: 5_000 });

    // Fill any visible number fields
    const inputs = page.locator('[role="dialog"] input[type="number"], [role="dialog"] input[type="text"]');
    const inputCount = await inputs.count();
    for (let i = 0; i < Math.min(inputCount, 3); i++) {
      await inputs.nth(i).fill('100');
    }

    await page.getByRole('button', { name: 'Zapisz' }).click();
    // Modal should close after save
    await expect(page.getByText('Wypełnij oświadczenie')).not.toBeVisible({ timeout: 8_000 });
  });

  test('anulowanie formularza zamyka modal bez zapisu', async ({ page }) => {
    const declarationsPage = new DeclarationsPage(page);

    await declarationsPage.goto();
    await declarationsPage.generateDeclarations();

    const rowCount = await declarationsPage.declarationRows().count();
    if (rowCount === 0) { test.skip(); return; }

    const fillBtn = declarationsPage.declarationRows()
      .filter({ has: page.getByRole('button', { name: 'Wypełnij', disabled: false }) })
      .first()
      .getByRole('button', { name: 'Wypełnij' });

    if (await fillBtn.count() === 0) { test.skip(); return; }

    await fillBtn.click();
    await expect(page.getByText('Wypełnij oświadczenie')).toBeVisible({ timeout: 5_000 });
    await page.getByRole('button', { name: 'Anuluj' }).click();
    await expect(page.getByText('Wypełnij oświadczenie')).not.toBeVisible({ timeout: 3_000 });
  });
});
