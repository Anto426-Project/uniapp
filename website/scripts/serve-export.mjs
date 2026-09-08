import { fileURLToPath } from 'node:url';
import { createServer } from 'node:http';
import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { resolve, extname, sep } from 'node:path';

const root = fileURLToPath(new URL('../out', import.meta.url));
const base = (process.env.NEXT_PUBLIC_BASE_PATH || '').replace(/\/$/, '');
const types = { '.html': 'text/html', '.css': 'text/css', '.js': 'text/javascript', '.json': 'application/json', '.png': 'image/png', '.jpg': 'image/jpeg', '.webp': 'image/webp', '.svg': 'image/svg+xml', '.woff2': 'font/woff2', '.ico': 'image/x-icon' };
createServer(async (request, response) => {
  try {
    const path = decodeURIComponent(new URL(request.url, 'http://localhost').pathname);
    if (path !== base && !path.startsWith(`${base}/`)) throw Error('Outside base path');
    let file = resolve(root, `.${path.slice(base.length) || '/'}`);
    if (file !== root && !file.startsWith(root + sep)) throw Error('Outside export');
    if ((await stat(file)).isDirectory()) file = resolve(file, 'index.html');
    response.writeHead(200, { 'Content-Type': types[extname(file)] || 'application/octet-stream' });
    createReadStream(file).pipe(response);
  } catch {
    response.writeHead(404).end('Not found');
  }
}).listen(Number(process.env.PORT || 4173), '127.0.0.1');
