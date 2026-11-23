import { fileURLToPath } from 'node:url';
import { mkdir, readdir, stat, writeFile } from 'node:fs/promises';
import { resolve, parse } from 'node:path';
import sharp from 'sharp';

const sourceDir = fileURLToPath(new URL('../public/assets/screenshots/', import.meta.url));
const previewDir = resolve(sourceDir, 'previews');
const manifestOut = fileURLToPath(new URL('../src/data/available-screenshots.json', import.meta.url));
const scriptMtime = (await stat(new URL(import.meta.url))).mtimeMs;

await mkdir(previewDir, { recursive: true });

const entries = (await readdir(sourceDir))
  .filter((name) => /\.(jpg|jpeg|png)$/i.test(name))
  .sort();

for (const filename of entries) {
  const inputPath = resolve(sourceDir, filename);
  const outputPath = resolve(previewDir, `${parse(filename).name}.webp`);
  const existing = await stat(outputPath).catch(() => null);
  const inputStat = await stat(inputPath);

  if (existing && existing.mtimeMs >= Math.max(scriptMtime, inputStat.mtimeMs)) {
    continue;
  }

  await sharp(inputPath)
    .rotate()
    .resize({ width: 520, withoutEnlargement: true })
    .webp({ quality: 86 })
    .toFile(outputPath);
}

await writeFile(manifestOut, `${JSON.stringify(entries, null, 2)}\n`, 'utf8');
console.log(`Prepared ${entries.length} screenshots and updated available-screenshots.json`);
