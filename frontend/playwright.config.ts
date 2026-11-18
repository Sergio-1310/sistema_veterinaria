import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  workers: 1,
  timeout: 120_000,
  reporter: 'list',
  use: {
    baseURL: 'http://127.0.0.1:14203',
    channel: process.env['PW_CHANNEL'] || 'chromium',
    headless: true,
  },
  webServer: {
    command: 'npm run start -- --host 127.0.0.1 --port 14203',
    url: 'http://127.0.0.1:14203',
    reuseExistingServer: false,
    timeout: 120_000,
    env: { HV_API_TARGET: 'http://127.0.0.1:18083', NG_CLI_ANALYTICS: 'false' },
  },
});
