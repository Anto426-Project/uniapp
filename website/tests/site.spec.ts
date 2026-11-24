import { test, expect } from '@playwright/test';

test('Pages assets load and only the ARM64 download is shown', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.url().startsWith('http://127.0.0.1') && response.status() >= 400) {
      errors.push(`${response.status()} ${response.url()}`);
    }
  });

  await page.route('**/update.json', (route) =>
    route.fulfill({
      json: {
        latestVersion: '2.1.2',
        latestVersionCode: 212,
        downloadUrl: 'https://example.test/app.apk',
        downloadUrlsByAbi: { 'arm64-v8a': 'https://example.test/app.apk' },
      },
    })
  );

  await page.goto('./');
  await expect(page.getByRole('link', { name: /Scarica APK ARM64/ })).toHaveAttribute(
    'href',
    'https://example.test/app.apk'
  );
  await expect(page.getByText('Universale', { exact: true })).toHaveCount(0);

  await page.locator('#screenshots').scrollIntoViewIfNeeded();
  await expect(page.locator('.screenshot-carousel img').first()).toHaveJSProperty('complete', true);
  expect(
    await page
      .locator('.screenshot-carousel img')
      .first()
      .evaluate((img: HTMLImageElement) => img.naturalWidth)
  ).toBeGreaterThan(0);

  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  expect(errors).toEqual([]);
});

test('navigation opens the selected screenshot and preserves it in the lightbox', async ({ page }) => {
  await page.goto('./');
  await page.locator('#screenshots').scrollIntoViewIfNeeded();
  const slideCount = await page.locator('.screenshot-carousel__dot').count();
  expect(slideCount).toBeGreaterThanOrEqual(7);

  await page.getByRole('tab', { name: 'Vai a Servizi e portali' }).click();
  await expect(page.locator('.screenshot-carousel__position')).toHaveText(`2 / ${slideCount}`);

  const selected = page.locator('[data-selected="true"] button');
  const previewSrc = (await selected.locator('img').getAttribute('src'))!;
  const fullSrc = previewSrc.replace('/previews/', '/').replace(/\.webp$/, '.jpg');

  await selected.click();
  await expect(page.getByRole('dialog').locator('img')).toHaveAttribute('src', fullSrc);
  await page.waitForTimeout(4700);
  await expect(page.getByRole('dialog').locator('img')).toHaveAttribute('src', fullSrc);

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
  const startX = box.x + box.width * 0.78;

  await session.send('Input.dispatchTouchEvent', {
    type: 'touchStart',
    touchPoints: [{ x: startX, y }],
  });
  for (let i = 1; i <= 10; i++) {
    await session.send('Input.dispatchTouchEvent', {
      type: 'touchMove',
      touchPoints: [{ x: startX - (box.width * 0.52 * i) / 10, y }],
    });
    await page.waitForTimeout(18);
  }
  await session.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });

  const slideCount = await page.locator('.screenshot-carousel__dot').count();
  await expect(page.locator('.screenshot-carousel__position')).not.toHaveText(`1 / ${slideCount}`);
  await expect(page.getByRole('dialog')).toHaveCount(0);
});

test('Android and iOS downloads retain independent versions and iOS packages', async ({ page }) => {
  const release = (
    version: string,
    variants: Record<string, { version: string; downloads: { name: string; os: string; url: string; sha256: string }[] }>
  ) => ({
    version,
    tag: `v${version}`,
    url: `https://example.test/releases/${version}`,
    signing: 'unsigned',
    variants,
  });

  const variant = (version: string, names: string[]) => ({
    version,
    downloads: names.map((name) => ({
      os: 'ios',
      name,
      url: `https://example.test/${name}`,
      sha256: 'a'.repeat(64),
    })),
  });

  const longNotes = Array.from({ length: 24 }, (_, i) => `- Novità #${i + 1}: dettaglio della modifica **release** \`v2.1.2\``).join('\n');

  await page.route('**/update.json', (route) =>
    route.fulfill({
      json: {
        latestVersion: '2.1.2',
        latestVersionCode: 212,
        downloadUrlsByAbi: { 'arm64-v8a': 'https://example.test/UniApp-android.apk' },
        notes: longNotes,
      },
    })
  );

  await page.route('**/platforms.json', (route) =>
    route.fulfill({
      json: {
        schema: 1,
        platforms: {
          ios: release('2.1.1', {
            ios: variant('2.1.1', ['UniApp-ios.ipa', 'UniApp-ios-simulator.zip']),
          }),
        },
      },
    })
  );

  await page.goto('./');
  const ios = page.getByRole('article', { name: 'Download iOS', exact: true });
  await expect(page.locator('#download .ver-text').first()).toHaveText('v2.1.2');
  await expect(page.getByRole('link', { name: /Scarica APK ARM64/ })).toHaveAttribute(
    'href',
    'https://example.test/UniApp-android.apk'
  );
  await expect(page.locator('#download').getByRole('article')).toHaveCount(1);
  await expect(ios.getByText('Versione 2.1.1')).toBeVisible();
  await expect(ios.getByRole('link', { name: 'IPA iOS', exact: true })).toHaveAttribute(
    'href',
    'https://example.test/UniApp-ios.ipa'
  );
  await expect(ios.getByRole('link', { name: 'Bundle simulatore', exact: true })).toHaveAttribute(
    'href',
    'https://example.test/UniApp-ios-simulator.zip'
  );
  await expect(ios.getByText(/firma e provisioning/)).toBeVisible();

  // Long 24-item changelog stays bounded and scrolls internally without stretching the page
  const scrollRegion = page.locator('.changelog-scroll-region');
  const scrollMetrics = await scrollRegion.evaluate((el) => ({
    clientHeight: el.clientHeight,
    scrollHeight: el.scrollHeight,
  }));
  expect(scrollMetrics.clientHeight).toBeLessThanOrEqual(480);
  expect(scrollMetrics.scrollHeight).toBeGreaterThan(scrollMetrics.clientHeight + 100);

  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
});

test('Segmented pill stays strictly inside track and switches themes, tabs, and Light/Dark cleanly', async ({ page }) => {
  await page.goto('./');

  const group = page.getByRole('group', { name: "Scegli il tema dell'anteprima" });
  await group.getByRole('button', { name: 'Viola' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'violet');
  await expect(page.locator('.triptych-center img')).toHaveAttribute('src', /violet-home\.webp$/);

  // Wait for spring settle then verify pixel-exact indicator containment
  await page.waitForTimeout(450);
  const geometry = await group.evaluate((container) => {
    const c = container.getBoundingClientRect();
    const ind = container.querySelector('.fluid-segmented__indicator')!.getBoundingClientRect();
    const btn = container.querySelector('[aria-pressed="true"]')!.getBoundingClientRect();
    return {
      insideLeft: ind.left >= c.left - 0.5,
      insideRight: ind.right <= c.right + 0.5,
      insideTop: ind.top >= c.top - 0.5,
      insideBottom: ind.bottom <= c.bottom + 0.5,
      matchesButtonTop: Math.abs(ind.top - btn.top) <= 1,
      matchesButtonBottom: Math.abs(ind.bottom - btn.bottom) <= 1,
    };
  });
  expect(geometry).toEqual({
    insideLeft: true,
    insideRight: true,
    insideTop: true,
    insideBottom: true,
    matchesButtonTop: true,
    matchesButtonBottom: true,
  });

  // Switch Architecture tab
  const archTabs = page.getByRole('group', { name: "Sezioni dell'architettura" });
  await archTabs.getByRole('button', { name: 'Sicurezza' }).click();
  await expect(page.getByText(/Sessioni isolate, biometria e revoca remota/)).toBeVisible();

  // Verify infinite circular wrap in both directions via keyboard arrows (1 -> N -> 1)
  const carousel = page.locator('.screenshot-carousel');
  await carousel.scrollIntoViewIfNeeded();
  const slideCount = await carousel.locator('.screenshot-carousel__dot').count();
  await expect(page.locator('.screenshot-carousel__position')).toHaveText(`1 / ${slideCount}`);
  await carousel.locator('[data-selected="true"] button').focus();
  await page.keyboard.press('ArrowLeft');
  await expect(page.locator('.screenshot-carousel__position')).toHaveText(`${slideCount} / ${slideCount}`);
  await page.keyboard.press('ArrowRight');
  await expect(page.locator('.screenshot-carousel__position')).toHaveText(`1 / ${slideCount}`);

  // Switch Light appearance
  await page.locator('header').getByRole('button', { name: 'Attiva aspetto chiaro' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-appearance', 'light');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
});

test('iOS Sideload Studio highlights Download in navbar and supports Apple ID, SideStore, and .p12 modes', async ({
  page,
  isMobile,
}) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.url().startsWith('http://127.0.0.1') && response.status() >= 400) {
      errors.push(`${response.status()} ${response.url()}`);
    }
  });

  await page.route('**/platforms.json', (route) =>
    route.fulfill({
      json: {
        schema: 1,
        platforms: {
          ios: {
            version: '2.1.2',
            tag: 'v2.1.2-ios',
            url: 'https://example.test/releases/v2.1.2-ios',
            signing: 'unsigned',
            variants: {
              ios: {
                version: '2.1.2',
                downloads: [
                  {
                    os: 'ios',
                    name: 'UniApp-ios-2.1.2-1001-unsigned.ipa',
                    url: 'https://github.com/Anto426-Project/uniapp-upstream/releases/download/v2.1.2-ios/UniApp-ios-2.1.2-1001-unsigned.ipa',
                    sha256: 'b'.repeat(64),
                  },
                ],
              },
            },
          },
        },
      },
    })
  );

  await page.route('https://relay.example.test/healthz', (route) =>
    route.fulfill({
      json: { ok: true, service: 'uniapp-ios-sideload-relay' },
    })
  );

  await page.goto('./');
  const iosCard = page.getByRole('article', { name: 'Download iOS', exact: true });
  await iosCard.getByRole('link', { name: /Sideload Web/i }).click();

  await expect(page).toHaveURL(/\/ios\/?$/);
  await expect(page.getByRole('heading', { name: /Installa su iPhone/i })).toBeVisible();

  // Navbar must NOT contain a separate Sideload iOS link, and Download stays active on desktop
  await expect(page.locator('header').getByText('Sideload iOS')).toHaveCount(0);
  if (!isMobile) {
    await expect(page.locator('header').getByRole('link', { name: 'Download', exact: true })).toHaveAttribute(
      'aria-current',
      'location'
    );
  }

  const modeGroup = page.getByRole('group', { name: 'Modalità di installazione iOS' });

  // No internal Cloudflare Worker / Relay configuration UI is exposed to the public
  await expect(page.getByText(/Cloudflare Worker/i)).toHaveCount(0);
  await expect(page.getByText(/Impostazioni Relay/i)).toHaveCount(0);

  // Switch to USB + Apple ID mode and verify fields
  await modeGroup.getByRole('button', { name: 'Cavo USB' }).click();
  await expect(page.getByLabel('Email Apple ID')).toBeVisible();
  const udidField = page.getByLabel('UDID dispositivo');
  if (!(await udidField.isVisible())) {
    await page.getByRole('button', { name: 'UDID manuale' }).click();
  }
  await expect(udidField).toBeVisible();

  // Switch to SideStore / AltStore mode and verify deep links
  await modeGroup.getByRole('button', { name: 'SideStore' }).click();
  await expect(page.getByRole('link', { name: 'Installa con SideStore' })).toHaveAttribute(
    'href',
    /^sidestore:\/\/install\?url=https%3A%2F%2Fgithub\.com/
  );
  await expect(page.getByRole('link', { name: 'Installa con AltStore' })).toHaveAttribute(
    'href',
    /^altstore:\/\/install\?url=https%3A%2F%2Fgithub\.com/
  );
  await expect(page.getByRole('link', { name: 'Aggiungi repo a SideStore' })).toHaveAttribute(
    'href',
    /^sidestore:\/\/source\?url=http/
  );

  // Switch to Offline .p12 mode
  await modeGroup.getByRole('button', { name: 'Certificato .p12' }).click();
  await expect(page.getByLabel('File certificato (.p12 / .pfx)')).toBeVisible();
  await expect(page.getByLabel('Profilo (.mobileprovision)')).toBeVisible();

  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  expect(errors).toEqual([]);
});
