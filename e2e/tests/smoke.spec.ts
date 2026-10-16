import { expect, test } from '@playwright/test';

test.describe('Smoke', { tag: ['@NFR-004'] }, () => {
  test('NFR-004/AC-6 the full stack is up and reports ready', async ({ request }) => {
    const response = await request.get('/q/health/ready');

    expect(response.ok()).toBeTruthy();
    expect(await response.json()).toMatchObject({ status: 'UP' });
  });

  test('NFR-004/AC-6 opening the gallery leads to the seeded Keycloak realm', async ({ page }) => {
    await page.goto('/');

    await expect(page).toHaveURL(/\/realms\/pic-torres\/protocol\/openid-connect\/auth/);
    await expect(page.locator('#username')).toBeVisible();
  });
});
