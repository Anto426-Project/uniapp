import { withBasePath } from '@/utils/basePath';
import { runtimeImportPublic } from './runtime-import';
import { IOS_RELAY_URL, toWispWebSocketUrl } from './relay-config';

export interface LibcurlApi {
  fetch(input: string | URL, init?: Record<string, unknown>): Promise<Response>;
  load_wasm(url?: string): Promise<void>;
  set_websocket(url: string): void;
  readonly ready?: boolean;
}

export interface AnisetteData {
  machineID: string;
  oneTimePassword: string;
  localUserID: string;
  routingInfo: number;
  deviceUniqueIdentifier: string;
  deviceDescription: string;
  deviceSerialNumber: string;
  date: Date;
  locale: string;
  timeZone: string;
}

interface AnisetteHttpClient {
  get(url: string, headers: Record<string, string>): Promise<Uint8Array>;
  post(url: string, body: string, headers: Record<string, string>): Promise<Uint8Array>;
}

interface AnisetteInstance {
  readonly isProvisioned: boolean;
  provision(): Promise<void>;
  getData(): Promise<Record<string, string>>;
}

interface AnisetteBrowserModule {
  Anisette: {
    fromSo(
      storeservicescore: Uint8Array,
      coreadi: Uint8Array,
      wasmModule: unknown,
      options: {
        httpClient: AnisetteHttpClient;
        init?: { libraryPath?: string };
      }
    ): Promise<AnisetteInstance>;
  };
  loadWasmModule(overrides?: Record<string, unknown>): Promise<unknown>;
}

let libcurlInstance: LibcurlApi | null = null;
let libcurlWasmLoadedPromise: Promise<LibcurlApi> | null = null;
let activeWispUrl = '';
let anisetteInstance: AnisetteInstance | null = null;

function resolveActiveWispUrl(overrideRelayUrl?: string): string {
  const base = overrideRelayUrl ?? IOS_RELAY_URL;
  const wsUrl = toWispWebSocketUrl(base);
  if (!wsUrl) {
    throw new Error(
      'Servizio di autenticazione Apple non attivo in questo ambiente. Usa SideStore / AltStore oppure firma offline con certificato .p12.'
    );
  }
  return wsUrl;
}

export async function initLibcurl(overrideRelayUrl?: string): Promise<LibcurlApi> {
  const wsUrl = resolveActiveWispUrl(overrideRelayUrl);

  if (!libcurlWasmLoadedPromise) {
    libcurlWasmLoadedPromise = (async () => {
      const mod = await runtimeImportPublic<{ libcurl?: LibcurlApi }>(
        '/ios-sideload/libcurl/libcurl.mjs'
      );
      if (!mod?.libcurl || typeof mod.libcurl.load_wasm !== 'function') {
        throw new Error('Modulo libcurl WASM non valido');
      }
      const api = mod.libcurl;
      await api.load_wasm(withBasePath('/ios-sideload/libcurl/libcurl.wasm'));
      libcurlInstance = api;
      return api;
    })().catch((err) => {
      libcurlWasmLoadedPromise = null;
      libcurlInstance = null;
      throw new Error(
        `Impossibile inizializzare libcurl WASM: ${err instanceof Error ? err.message : String(err)}`
      );
    });
  }

  const api = await libcurlWasmLoadedPromise;
  if (activeWispUrl !== wsUrl) {
    api.set_websocket(wsUrl);
    activeWispUrl = wsUrl;
  }
  return api;
}

export function requireLibcurl(): LibcurlApi {
  if (!libcurlInstance) {
    throw new Error('libcurl non è pronto: esegui prima initLibcurl().');
  }
  return libcurlInstance;
}

class LibcurlHttpClient implements AnisetteHttpClient {
  async get(url: string, headers: Record<string, string>): Promise<Uint8Array> {
    const curl = await initLibcurl();
    const response = await curl.fetch(url, {
      method: 'GET',
      headers,
      insecure: true,
      _libcurl_http_version: 1.1,
    });
    if (!response.ok) {
      throw new Error(`Richiesta Anisette GET fallita (${response.status})`);
    }
    return new Uint8Array(await response.arrayBuffer());
  }

  async post(url: string, body: string, headers: Record<string, string>): Promise<Uint8Array> {
    const curl = await initLibcurl();
    const response = await curl.fetch(url, {
      method: 'POST',
      body,
      headers,
      insecure: true,
      _libcurl_http_version: 1.1,
    });
    if (!response.ok) {
      throw new Error(`Richiesta Anisette POST fallita (${response.status})`);
    }
    return new Uint8Array(await response.arrayBuffer());
  }
}

export async function initAnisette(overrideRelayUrl?: string): Promise<AnisetteInstance> {
  await initLibcurl(overrideRelayUrl);
  if (anisetteInstance) {
    return anisetteInstance;
  }

  const [anisetteMod, storeservicescore, coreadi] = await Promise.all([
    runtimeImportPublic<AnisetteBrowserModule>('/ios-sideload/anisette/browser.js'),
    fetch(withBasePath('/ios-sideload/anisette/libstoreservicescore.so')).then(async (r) => {
      if (!r.ok) throw new Error(`Download libstoreservicescore.so fallito (${r.status})`);
      return new Uint8Array(await r.arrayBuffer());
    }),
    fetch(withBasePath('/ios-sideload/anisette/libCoreADI.so')).then(async (r) => {
      if (!r.ok) throw new Error(`Download libCoreADI.so fallito (${r.status})`);
      return new Uint8Array(await r.arrayBuffer());
    }),
  ]);

  const wasmModule = await anisetteMod.loadWasmModule();
  const httpClient = new LibcurlHttpClient();
  anisetteInstance = await anisetteMod.Anisette.fromSo(storeservicescore, coreadi, wasmModule, {
    httpClient,
    init: {
      libraryPath: './anisette/',
    },
  });

  return anisetteInstance;
}

export async function getAnisetteData(
  onLog?: (msg: string) => void,
  overrideRelayUrl?: string
): Promise<AnisetteData> {
  const log = onLog ?? (() => undefined);
  const anisette = await initAnisette(overrideRelayUrl);

  if (!anisette.isProvisioned) {
    log('anisette: provisioning attestazione dispositivo Apple in WebAssembly...');
    await anisette.provision();
    log('anisette: provisioning completato');
  }

  const headers = await anisette.getData();
  return {
    machineID: headers['X-Apple-I-MD-M'],
    oneTimePassword: headers['X-Apple-I-MD'],
    localUserID: headers['X-Apple-I-MD-LU'],
    routingInfo: Number.parseInt(headers['X-Apple-I-MD-RINFO'], 10),
    deviceUniqueIdentifier: headers['X-Mme-Device-Id'],
    deviceDescription: headers['X-MMe-Client-Info'],
    deviceSerialNumber: headers['X-Apple-I-SRL-NO'] || '0',
    date: new Date(headers['X-Apple-I-Client-Time']),
    locale: headers['X-Apple-Locale'],
    timeZone: headers['X-Apple-I-TimeZone'],
  };
}

export function resetAnisetteSession(): void {
  anisetteInstance = null;
}
