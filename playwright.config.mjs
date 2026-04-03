import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/frontend',
  testMatch: ['**/desktop-visual.spec.mjs'],
  snapshotPathTemplate: '{testDir}/{testFilePath}-snapshots/{arg}{ext}',
  timeout: 60_000,
  expect: {
    timeout: 10_000,
    toHaveScreenshot: {
      animations: 'disabled',
      scale: 'css'
    }
  },
  projects: [
    {
      name: 'desktop-chromium',
      use: {
        baseURL: 'http://127.0.0.1:4173',
        browserName: 'chromium',
        ...devices['Desktop Chrome'],
        deviceScaleFactor: 1,
        colorScheme: 'light',
        timezoneId: 'Europe/London',
        locale: 'en-GB'
      }
    }
  ],
  webServer: {
    command: 'npm run build && node scripts/portable-vite.mjs preview --host 127.0.0.1 --port 4173 --strictPort',
    port: 4173,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000
  }
});
