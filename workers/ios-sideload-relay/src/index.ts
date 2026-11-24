import { connect } from 'cloudflare:sockets';
import { server as wisp } from '@mercuryworkshop/wisp-js/server';

export interface Env {
  ALLOWED_ORIGINS?: string;
}

// Strictly allowlist ONLY Apple's official authentication & provisioning endpoints on TLS port 443.
const APPLE_HOSTNAME_WHITELIST = [
  /^gsa\.apple\.com$/,
  /^idmsa\.apple\.com$/,
  /^developerservices2\.apple\.com$/,
  /^auth\.itunes\.apple\.com$/,
  /^buy\.itunes\.apple\.com$/,
  /^init\.itunes\.apple\.com$/,
  /^p\d+-buy\.itunes\.apple\.com$/,
];

// Strictly allowlist ONLY official UniApp GitHub Release IPA downloads for CORS streaming.
const OFFICIAL_IPA_URL_RE =
  /^https:\/\/github\.com\/Anto426-Project\/(?:uniapp-upstream|Uniapp)\/releases\/download\/[^/]+\/[^/]+\.ipa$/i;

wisp.options.hostname_whitelist = APPLE_HOSTNAME_WHITELIST;
wisp.options.port_whitelist = [443];
wisp.options.allow_direct_ip = false;
wisp.options.allow_private_ips = false;
wisp.options.allow_loopback_ips = false;
wisp.options.allow_udp_streams = false;
wisp.options.allow_tcp_streams = true;
wisp.options.wisp_version = 1;

class AsyncQueue<T> {
  private queue: T[] = [];
  private resolvers: Array<(value: T | null) => void> = [];
  private closed = false;

  put(value: T): void {
    if (this.closed) return;
    const resolver = this.resolvers.shift();
    if (resolver) {
      resolver(value);
      return;
    }
    this.queue.push(value);
  }

  async get(): Promise<T | null> {
    if (this.queue.length > 0) return this.queue.shift() ?? null;
    if (this.closed) return null;
    return new Promise<T | null>((resolve) => {
      this.resolvers.push(resolve);
    });
  }

  close(): void {
    if (this.closed) return;
    this.closed = true;
    while (this.resolvers.length > 0) {
      this.resolvers.shift()?.(null);
    }
    this.queue.length = 0;
  }
}

class WorkerTCPSocket {
  hostname: string;
  port: number;
  private socket: Socket | null = null;
  private reader: ReadableStreamDefaultReader<Uint8Array> | null = null;
  private writer: WritableStreamDefaultWriter<Uint8Array> | null = null;
  private recvQueue = new AsyncQueue<Uint8Array>();

  constructor(hostname: string, port: number) {
    this.hostname = hostname;
    this.port = port;
  }

  async connect(): Promise<void> {
    // secureTransport: "off" keeps TLS end-to-end between the user's browser (libcurl-wasm) and Apple.
    this.socket = connect(
      { hostname: this.hostname, port: this.port },
      { secureTransport: 'off', allowHalfOpen: false }
    );
    this.reader = this.socket.readable.getReader();
    this.writer = this.socket.writable.getWriter();
    void this.pumpReadable();
  }

  private async pumpReadable(): Promise<void> {
    if (!this.reader) return;
    try {
      while (true) {
        const { value, done } = await this.reader.read();
        if (done) break;
        if (value) {
          this.recvQueue.put(value instanceof Uint8Array ? value : new Uint8Array(value));
        }
      }
    } catch {
      // Surface stream closure cleanly
    } finally {
      this.recvQueue.close();
      await this.close();
    }
  }

  async recv(): Promise<Uint8Array | null> {
    return this.recvQueue.get();
  }

  async send(data: Uint8Array | ArrayBuffer | ArrayBufferView): Promise<void> {
    if (!this.writer) throw new Error('TCP socket not connected');
    const chunk =
      data instanceof Uint8Array
        ? data
        : data instanceof ArrayBuffer
          ? new Uint8Array(data)
          : new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    await this.writer.write(chunk);
  }

  async close(): Promise<void> {
    this.recvQueue.close();
    try {
      this.reader?.releaseLock();
    } catch {
      // Ignore
    }
    try {
      await this.writer?.close();
    } catch {
      // Ignore
    }
    try {
      await this.socket?.close();
    } catch {
      // Ignore
    }
    this.reader = null;
    this.writer = null;
    this.socket = null;
  }

  pause(): void {}
  resume(): void {}
}

class UnsupportedUDPSocket {
  hostname: string;
  port: number;
  constructor(hostname: string, port: number) {
    this.hostname = hostname;
    this.port = port;
  }
  async connect(): Promise<void> {
    throw new Error('UDP disabled');
  }
  async recv(): Promise<null> {
    return null;
  }
  async send(): Promise<void> {
    throw new Error('UDP disabled');
  }
  async close(): Promise<void> {}
}

class WorkerWebSocketCompat {
  private socket: WebSocket;
  binaryType = 'arraybuffer';
  onmessage: ((event: MessageEvent) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onerror: ((event: Event) => void) | null = null;

  constructor(socket: WebSocket) {
    this.socket = socket;
    this.socket.addEventListener('message', (e) => this.onmessage?.(e));
    this.socket.addEventListener('close', (e) => this.onclose?.(e));
    this.socket.addEventListener('error', (e) => this.onerror?.(e));
  }

  get readyState(): number {
    return this.socket.readyState;
  }

  get bufferedAmount(): number {
    return 0;
  }

  send(data: ArrayBuffer | ArrayBufferView | string): void {
    this.socket.send(data);
  }

  close(code?: number, reason?: string): void {
    this.socket.close(code, reason);
  }
}

function resolveAllowedOrigin(request: Request, env: Env): string | null {
  const reqOrigin = (request.headers.get('Origin') || '').trim().replace(/\/+$/, '');
  if (!reqOrigin) return null;

  const rawList = (env.ALLOWED_ORIGINS || 'https://anto426-project.github.io')
    .split(',')
    .map((o) => o.trim().replace(/\/+$/, ''))
    .filter(Boolean);

  return rawList.includes(reqOrigin) ? reqOrigin : null;
}

function corsHeaders(allowedOrigin: string): Record<string, string> {
  return {
    'Access-Control-Allow-Origin': allowedOrigin,
    'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Expose-Headers': 'Content-Length, Content-Type',
    Vary: 'Origin',
  };
}

function jsonResponse(
  status: number,
  body: unknown,
  extraHeaders: Record<string, string> = {}
): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'content-type': 'application/json; charset=utf-8',
      'cache-control': 'no-store',
      ...extraHeaders,
    },
  });
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const allowedOrigin = resolveAllowedOrigin(request, env);
    if (!allowedOrigin) {
      return new Response('Forbidden', { status: 403 });
    }

    const cors = corsHeaders(allowedOrigin);

    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: cors });
    }

    const url = new URL(request.url);

    if (url.pathname === '/healthz') {
      return jsonResponse(200, { ok: true }, cors);
    }

    if (url.pathname === '/ipa-proxy') {
      const target = (url.searchParams.get('url') || '').trim();
      if (!OFFICIAL_IPA_URL_RE.test(target)) {
        return jsonResponse(
          403,
          { ok: false, error: 'Solo gli IPA ufficiali di UniApp su GitHub Releases sono consentiti.' },
          cors
        );
      }

      const upstream = await fetch(target, { redirect: 'follow' });
      if (!upstream.ok) {
        return jsonResponse(upstream.status, { ok: false, error: `Upstream HTTP ${upstream.status}` }, cors);
      }

      const headers = new Headers(cors);
      headers.set('content-type', 'application/octet-stream');
      headers.set('cache-control', 'public, max-age=300');
      const len = upstream.headers.get('content-length');
      if (len) headers.set('content-length', len);

      return new Response(upstream.body, { status: 200, headers });
    }

    if (url.pathname === '/wisp' || url.pathname === '/wisp/') {
      const upgrade = request.headers.get('Upgrade');
      if (!upgrade || upgrade.toLowerCase() !== 'websocket') {
        return jsonResponse(426, { ok: false, error: 'WebSocket upgrade required' }, cors);
      }

      const pair = new WebSocketPair();
      const [client, server] = [pair[0], pair[1]];
      server.accept();

      ctx.waitUntil(
        (async () => {
          try {
            const compat = new WorkerWebSocketCompat(server);
            const conn = new wisp.ServerConnection(compat as unknown as WebSocket, '/wisp/', {
              TCPSocket: WorkerTCPSocket,
              UDPSocket: UnsupportedUDPSocket,
              ping_interval: 30,
              wisp_version: 1,
            });
            await conn.setup();
            await conn.run();
          } catch {
            try {
              server.close(1011, 'Wisp session closed');
            } catch {
              // Ignore
            }
          }
        })()
      );

      return new Response(null, { status: 101, webSocket: client });
    }

    return jsonResponse(404, { ok: false, error: 'Not found' }, cors);
  },
};
