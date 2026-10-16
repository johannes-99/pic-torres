import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.E2E_BASE_URL ?? 'http://localhost:8080';

/**
 * E2E tests (NFR-004 AC-6). By default Playwright starts the app in Quarkus dev mode, where Dev
 * Services provide Postgres and Keycloak with the seeded realm (users alice/bob/carol).
 * Set E2E_BASE_URL to test an already running stack instead (e.g. the production compose stack).
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['html', { open: 'never' }], ['list']] : 'list',
  use: {
    baseURL,
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
        command: './mvnw -B -ntp quarkus:dev -Dquarkus.analytics.disabled=true -Ddebug=false',
        cwd: '../backend',
        url: `${baseURL}/q/health/ready`,
        reuseExistingServer: !process.env.CI,
        timeout: 600_000,
        stdout: 'pipe',
      },
});
