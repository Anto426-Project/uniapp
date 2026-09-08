import { fileURLToPath } from 'node:url';
import { readFile, stat } from 'node:fs/promises';
import { resolve, sep } from 'node:path';

const base = (process.env.NEXT_PUBLIC_BASE_PATH || '').replace(/\/$/, '');
const root = fileURLToPath(new URL('../out', import.meta.url));
const html = await readFile(resolve(root, 'index.html'), 'utf8');
const paths = new Set([...html.matchAll(/(?:src|href)="([^"]+)"/g)].map((match) => match[1])
  .filter((path) => path.startsWith('/') && !path.startsWith('//')));
if (![...paths].some((path) => path.includes('/_next/static/'))) throw Error('No Next.js assets in export');
for (const path of paths) {
  const url = new URL(path, 'https://example.test');
  if (!url.pathname.startsWith(`${base}/`)) throw Error(`Wrong Pages base path: ${path}`);
  let file = resolve(root, '.' + decodeURIComponent(url.pathname.slice(base.length)));
  if (!file.startsWith(root + sep)) throw Error(`Outside export: ${path}`);
  if ((await stat(file)).isDirectory()) file = resolve(file, 'index.html');
  if (!(await stat(file)).isFile()) throw Error(`Missing exported asset: ${path}`);
}
JSON.parse(await readFile(resolve(root, 'update.json'), 'utf8'));
console.log(`Static export: ${paths.size} local asset paths verified for ${base || '/'}`);
