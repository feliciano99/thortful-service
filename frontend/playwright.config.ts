import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.E2E_BASE_URL ?? 'http://localhost:8080';
const user = process.env.APP_AUTH_USER ?? 'admin';
const password = process.env.APP_AUTH_PASSWORD ?? 'changeme';
const authToken = 'Basic ' + Buffer.from(`${user}:${password}`).toString('base64');

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL,
       extraHTTPHeaders: { Authorization: authToken },
    trace: 'on-first-retry'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } }
  ]
});
