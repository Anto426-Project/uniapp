import { fileURLToPath } from 'node:url';
import { mkdir, readdir, stat } from 'node:fs/promises';
import { resolve, parse } from 'node:path';
import sharp from 'sharp';

const source = fileURLToPath(new URL('../public/assets/screenshots/', import.meta.url));
const output = resolve(source, 'previews');
const scriptTime = (await stat(new URL(import.meta.url))).mtimeMs;
await mkdir(output, { recursive: true });
for (const name of await readdir(source)) {
  if (!/\.(jpg|jpeg|png)$/i.test(name)) continue;
  const input = resolve(source, name);
  const target = resolve(output, parse(name).name + '.webp');
  const existing = await stat(target).catch(() => null);
  if (existing && existing.mtimeMs >= Math.max(scriptTime, (await stat(input)).mtimeMs)) continue;
  // The full resolution image is reserved for the lightbox. Process sequentially
  // to keep memory use bounded on CI and avoid decoding every original in the browser.
  await sharp(input).rotate().resize({ width: 480, withoutEnlargement: true })
    .webp({ quality: 85 }).toFile(target);
}
