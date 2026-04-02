import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { ContractorTypesPage } from '../pages/ContractorTypesPage';

const TEST_SYMBOL = 'E2E_TST';
const TEST_NAME = 'Test E2E Typ';

test.describe('Administracja — typy kontrahentów', () => {
  test.beforeEach(async ({ page }) => {
    await new LoginPage(page).loginAs('admin@pse.pl', 'admin123');
  });

  test('strona wyświetla listę typów kontrahentów', async ({ page }) => {
    const contractorTypesPage = new ContractorTypesPage(page);

    await contractorTypesPage.goto();
    await contractorTypesPage.expectHeading();
    expect(await contractorTypesPage.rows().count()).toBeGreaterThan(0);
  });

  test('można dodać nowy typ kontrahenta', async ({ page }) => {
    const contractorTypesPage = new ContractorTypesPage(page);

    await contractorTypesPage.goto();
    await contractorTypesPage.openAddDialog();
    await contractorTypesPage.fillForm(TEST_SYMBOL, TEST_NAME);
    await contractorTypesPage.saveForm();
    await contractorTypesPage.expectRowWithSymbol(TEST_SYMBOL);
  });

  test('można edytować istniejący typ kontrahenta', async ({ page }) => {
    const contractorTypesPage = new ContractorTypesPage(page);
    const updatedName = TEST_NAME + ' (edytowany)';

    await contractorTypesPage.goto();
    await contractorTypesPage.editRowWithSymbol(TEST_SYMBOL);
    await page.fill('#name', updatedName);
    await contractorTypesPage.saveForm();

    await expect(page.getByText(updatedName)).toBeVisible({ timeout: 5_000 });
  });

  test('można usunąć nigsystemowy typ kontrahenta', async ({ page }) => {
    const contractorTypesPage = new ContractorTypesPage(page);

    await contractorTypesPage.goto();
    const countBefore = await contractorTypesPage.rows().count();
    await contractorTypesPage.deleteRowWithSymbol(TEST_SYMBOL);
    await page.waitForTimeout(500);

    const countAfter = await contractorTypesPage.rows().count();
    expect(countAfter).toBe(countBefore - 1);
  });

  test('anulowanie formularza nie dodaje nowego wpisu', async ({ page }) => {
    const contractorTypesPage = new ContractorTypesPage(page);

    await contractorTypesPage.goto();
    const countBefore = await contractorTypesPage.rows().count();
    await contractorTypesPage.openAddDialog();
    await contractorTypesPage.fillForm('CANCEL', 'Anulowany wpis');
    await contractorTypesPage.cancelForm();

    const countAfter = await contractorTypesPage.rows().count();
    expect(countAfter).toBe(countBefore);
  });
});
