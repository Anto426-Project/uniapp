import { test, expect } from '@playwright/test';

test('Pages assets load and only the ARM64 download is shown', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.url().startsWith('http://127.0.0.1') && response.status() >= 400) errors.push(response.url());
  });
  await page.route('**/update.json', (route) => route.fulfill({ json: {
    latestVersion: '2.0.2', latestVersionCode: 202,
    downloadUrl: 'https://example.test/app.apk',
    downloadUrlsByAbi: { 'arm64-v8a': 'https://example.test/app.apk' },
  } }));
  await page.goto('./');
  await expect(page.getByRole('link', { name: /Scarica APK ARM64/ })).toHaveAttribute('href', 'https://example.test/app.apk');
  await expect(page.getByText('Universale', { exact: true })).toHaveCount(0);
  await page.locator('#screenshots').scrollIntoViewIfNeeded();
  await expect(page.locator('.screenshot-carousel img').first()).toHaveJSProperty('complete', true);
  expect(await page.locator('.screenshot-carousel img').first().evaluate((img: HTMLImageElement) => img.naturalWidth)).toBeGreaterThan(0);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  expect(errors).toEqual([]);
});

test('navigation opens the selected screenshot and preserves it in the lightbox', async ({ page }) => {
  await page.goto('./');
  await page.getByRole('button', { name: 'Schermata successiva', exact: true }).click();
  await expect(page.locator('.screenshot-carousel__position')).toHaveText('2 / 8');
  const selected = page.locator('[data-selected="true"] button');
  const source = (await selected.locator('img').getAttribute('src'))!.replace('/previews/', '/').replace(/\.webp$/, '.jpg');
  await selected.click();
  await expect(page.getByRole('dialog').locator('img')).toHaveAttribute('src', source!);
  await page.waitForTimeout(4700);
  await expect(page.getByRole('dialog').locator('img')).toHaveAttribute('src', source!);
  await page.getByRole('button', { name: 'Chiudi anteprima' }).click();
  await expect(page.getByRole('dialog')).toHaveCount(0);
});

test('reduced motion disables autoplay and vertical scrolling stays available', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('./');
  await page.locator('.screenshot-carousel').scrollIntoViewIfNeeded();
  await expect(page.getByRole('button', { name: /scorrimento automatico/ })).toHaveCount(0);
  const index = await page.locator('.screenshot-carousel__position').textContent();
  await page.waitForTimeout(4700);
  await expect(page.locator('.screenshot-carousel__position')).toHaveText(index!);
  const before = await page.evaluate(() => window.scrollY);
  await page.locator('.screenshot-carousel__viewport').hover();
  await page.mouse.wheel(0, 350);
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBeGreaterThan(before + 100);
});

test('touch swipe selects a slide without opening the lightbox', async ({ page, context, isMobile }) => {
  test.skip(!isMobile, 'Touch gesture regression on mobile');
  await page.goto('./');
  const viewport = page.locator('.screenshot-carousel__viewport');
  await viewport.scrollIntoViewIfNeeded();
  const box = (await viewport.boundingBox())!;
  const session = await context.newCDPSession(page);
  const y = Math.max(80, box.y + box.height / 2);
  const startX = box.x + box.width * 0.75;
  await session.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: startX, y }] });
  for (let i = 1; i <= 10; i++) {
    await session.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: startX - box.width * 0.5 * i / 10, y }] });
    await page.waitForTimeout(20);
  }
  await session.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
  await expect(page.locator('.screenshot-carousel__position')).not.toHaveText('1 / 8');
  await expect(page.getByRole('dialog')).toHaveCount(0);
});
