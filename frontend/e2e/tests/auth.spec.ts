import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

test.describe('Uwierzytelnianie', () => {
  test('administrator może się zalogować i widzi swój panel', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const dashboard = new DashboardPage(page);

    await loginPage.loginAs('admin@pse.pl', 'admin123');
    await dashboard.expectUsername('Admin PSE');
    await expect(dashboard.sidebarLink('Typy oświadczeń')).toBeVisible();
    await expect(dashboard.sidebarLink('Typy kontrahentów')).toBeVisible();
    await expect(dashboard.sidebarLink('Przypisanie typów')).toBeVisible();
  });

  test('kontrahent może się zalogować i widzi panel rozliczeń', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const dashboard = new DashboardPage(page);

    await loginPage.loginAs('jan.kowalski@tauron.pl', 'jan123');
    await dashboard.expectUsername('Jan Kowalski');
    await expect(dashboard.sidebarLink('Niezłożone')).toBeVisible();
    // Admin links should not appear for Kontrahent
    await expect(dashboard.sidebarLink('Typy oświadczeń')).not.toBeVisible();
  });

  test('kontrahent nie ma dostępu do stron administracyjnych', async ({ page }) => {
    const loginPage = new LoginPage(page);

    await loginPage.loginAs('jan.kowalski@tauron.pl', 'jan123');
    await page.goto('/admin/contractor-types');
    // ProtectedRoute redirects unauthorized users back to /
    await expect(page).toHaveURL('http://localhost:5173/', { timeout: 5_000 });
  });
});
