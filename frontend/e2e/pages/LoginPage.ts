import { Page } from '@playwright/test';

export class LoginPage {
  constructor(private readonly page: Page) {}

  async loginAs(username: string, password: string) {
    await this.page.goto('/');
    // Wait for Keycloak redirect
    await this.page.waitForURL(/localhost:8180/, { timeout: 10_000 });
    await this.page.fill('#username', username);
    await this.page.fill('#password', password);
    await this.page.click('#kc-login');
    // Wait until redirected back to the app
    await this.page.waitForURL(/localhost:5173/, { timeout: 10_000 });
  }

  async logout() {
    await this.page.getByRole('button', { name: 'Wyloguj' }).click();
    await this.page.waitForURL(/localhost:8180/, { timeout: 10_000 });
  }
}
