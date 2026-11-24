import { fileURLToPath } from 'node:url';
import { createServer } from 'node:http';
import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { resolve, extname, sep } from 'node:path';

const root = fileURLToPath(new URL('../out', import.meta.url));
const base = (process.env.NEXT_PUBLIC_BASE_PATH || '').replace(/\/$/, '');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.wasm': 'application/wasm',
  '.so': 'application/octet-stream',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.webp': 'image/webp',
  '.svg': 'image/svg+xml',
  '.woff2': 'font/woff2',
  '.ico': 'image/x-icon',
};

const port = Number(process.env.PORT || 4173);

createServer(async (req, res) => {
  try {
    const pathname = decodeURIComponent(new URL(req.url || '/', 'http://localhost').pathname);
    if (base && pathname !== base && !pathname.startsWith(`${base}/`)) {
      throw new Error('Outside basePath');
    }
    const relative = base ? pathname.slice(base.length) || '/' : pathname;
    let filePath = resolve(root, `.${relative}`);
    if (filePath !== root && !filePath.startsWith(root + sep)) {
      throw new Error('Outside export root');
    }
    if ((await stat(filePath)).isDirectory()) {
      filePath = resolve(filePath, 'index.html');
    }
    res.writeHead(200, { 'Content-Type': MIME_TYPES[extname(filePath)] || 'application/octet-stream' });
    createReadStream(filePath).pipe(res);
  } catch {
    res.writeHead(404).end('Not found');
  }
}).listen(port, '127.0.0.1');
