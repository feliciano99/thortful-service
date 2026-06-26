import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.E2E_BASE_URL ?? 'http://localhost:8080';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL,
    // Native HTTP Basic auth, mapped from environment variables, so headless
    // runs are authenticated at the network layer and never hit the login form.
    httpCredentials: {
      username: process.env.APP_AUTH_USER ?? 'admin',
      password: process.env.APP_AUTH_PASSWORD ?? 'changeme',
      origin: baseURL,
      send: 'always'
    },
    trace: 'on-first-retry'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } }
  ]
});
