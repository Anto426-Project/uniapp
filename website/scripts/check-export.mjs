import { fileURLToPath } from 'node:url';
import { readFile, stat } from 'node:fs/promises';
import { resolve, sep } from 'node:path';

const base = (process.env.NEXT_PUBLIC_BASE_PATH || '').replace(/\/$/, '');
const root = fileURLToPath(new URL('../out', import.meta.url));
const html = await readFile(resolve(root, 'index.html'), 'utf8');

const paths = new Set(
  [...html.matchAll(/(?:src|href)="([^"]+)"/g)]
    .map((match) => match[1])
    .filter((path) => path.startsWith('/') && !path.startsWith('//'))
);

if (![...paths].some((path) => path.includes('/_next/static/'))) {
  throw new Error('No Next.js static assets found in export');
}

for (const path of paths) {
  const url = new URL(path, 'https://example.test');
  if (!url.pathname.startsWith(`${base}/`)) {
    throw new Error(`Asset outside configured basePath (${base}): ${path}`);
  }
  let target = resolve(root, '.' + decodeURIComponent(url.pathname.slice(base.length)));
  if (!target.startsWith(root + sep)) {
    throw new Error(`Path escapes export directory: ${path}`);
  }
  if ((await stat(target)).isDirectory()) {
    target = resolve(target, 'index.html');
  }
  if (!(await stat(target)).isFile()) {
    throw new Error(`Missing exported asset: ${path}`);
  }
}

JSON.parse(await readFile(resolve(root, 'update.json'), 'utf8'));
JSON.parse(await readFile(resolve(root, 'platforms.json'), 'utf8'));
console.log(`Static export verified: ${paths.size} asset paths valid for ${base || '/'}`);
