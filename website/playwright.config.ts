import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  use: {
    baseURL: 'http://127.0.0.1:4173/uniapp-upstream/',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'mobile', use: { ...devices['Pixel 7'], defaultBrowserType: 'chromium' } },
    { name: 'narrow', use: { viewport: { width: 320, height: 640 }, isMobile: true, hasTouch: true } },
    { name: 'desktop', use: { viewport: { width: 1440, height: 900 } } },
  ],
  webServer: {
    command: 'node scripts/serve-export.mjs',
    env: { NEXT_PUBLIC_BASE_PATH: '/uniapp-upstream' },
    url: 'http://127.0.0.1:4173/uniapp-upstream/',
    reuseExistingServer: false,
  },
});
