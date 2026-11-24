import JSZip from 'jszip';
import forge from 'node-forge';
import { withBasePath } from '@/utils/basePath';

interface ZsignEmscriptenFs {
  mkdirTree(path: string): void;
  writeFile(path: string, data: Uint8Array, opts?: { canOwn?: boolean }): void;
  readFile(path: string, opts: { encoding: 'binary' }): Uint8Array;
  readdir(path: string): string[];
  stat(path: string): { mode: number };
  isDir(mode: number): boolean;
  isFile(mode: number): boolean;
  analyzePath(path: string): { exists: boolean };
  rmdir(path: string): void;
  unlink(path: string): void;
}

interface RawZsignClient {
  mod: { FS?: ZsignEmscriptenFs };
  version(): string;
  signBundle(
    inputFolder: string,
    options: {
      certFile?: string;
      pkeyFile?: string;
      provFile?: string;
      password?: string;
      entitlementsFile?: string;
      bundleId?: string;
      bundleVersion?: string;
      displayName?: string;
      adhoc?: boolean;
      sha256Only?: boolean;
      forceSign?: boolean;
      weakInject?: boolean;
      enableCache?: boolean;
    }
  ): number;
}

interface ZsignBundleExports {
  ZsignWasmClient: {
    create(opts: {
      moduleFactory: (opts?: Record<string, unknown>) => Promise<unknown>;
      moduleOptions?: Record<string, unknown>;
    }): Promise<RawZsignClient>;
  };
  createZsignModule?: (opts?: Record<string, unknown>) => Promise<unknown>;
  createEmbeddedZsignModule?: (opts?: Record<string, unknown>) => Promise<unknown>;
}

let zsignBundlePromise: Promise<ZsignBundleExports> | null = null;

function resolveBundleExports(input: unknown): ZsignBundleExports | null {
  if (input && typeof input === 'object' && 'default' in input) {
    const nested = resolveBundleExports((input as { default: unknown }).default);
    if (nested) return nested;
  }
  if (!input || typeof input !== 'object') return null;
  if (typeof (input as { ZsignWasmClient?: unknown }).ZsignWasmClient === 'function') {
    return input as ZsignBundleExports;
  }
  return null;
}

async function loadZsignBundle(): Promise<ZsignBundleExports> {
  if (!zsignBundlePromise) {
    zsignBundlePromise = (async () => {
      const bundleUrl = withBasePath('/ios-sideload/zsign/zsign-wasm.min.js');
      const response = await fetch(bundleUrl);
      if (!response.ok) {
        throw new Error(`Impossibile caricare il modulo zsign WASM (${response.status})`);
      }
      const source = await response.text();
      const cjsModule: { exports: unknown } = { exports: {} };
      const evaluate = new Function(
        'module',
        'exports',
        'require',
        '__filename',
        '__dirname',
        'globalThis',
        source
      );
      evaluate(cjsModule, cjsModule.exports, undefined, bundleUrl, '', globalThis);
      const resolved = resolveBundleExports(cjsModule.exports);
      if (!resolved) {
        throw new Error('Modulo zsign WASM non valido');
      }
      return resolved;
    })().catch((err) => {
      zsignBundlePromise = null;
      throw err;
    });
  }
  return zsignBundlePromise;
}

function normalizeZipPath(p: string): string {
  return String(p || '')
    .replace(/\\/g, '/')
    .replace(/^\/+/, '');
}

function writeFsFile(fs: ZsignEmscriptenFs, outPath: string, data: Uint8Array): void {
  const idx = outPath.lastIndexOf('/');
  if (idx > 0) {
    fs.mkdirTree(outPath.slice(0, idx));
  }
  fs.writeFile(outPath, data, { canOwn: true });
}

function walkFsFiles(
  fs: ZsignEmscriptenFs,
  rootPath: string,
  onFile: (absPath: string) => void
): void {
  const entries = fs.readdir(rootPath);
  for (const name of entries) {
    if (name === '.' || name === '..') continue;
    const fullPath = `${rootPath}/${name}`;
    const st = fs.stat(fullPath);
    if (fs.isDir(st.mode)) {
      walkFsFiles(fs, fullPath, onFile);
    } else if (fs.isFile(st.mode)) {
      onFile(fullPath);
    }
  }
}

function rmrfFs(fs: ZsignEmscriptenFs, pathname: string): void {
  const info = fs.analyzePath(pathname);
  if (!info.exists) return;
  const st = fs.stat(pathname);
  if (fs.isDir(st.mode)) {
    for (const name of fs.readdir(pathname)) {
      if (name === '.' || name === '..') continue;
      rmrfFs(fs, `${pathname}/${name}`);
    }
    fs.rmdir(pathname);
  } else {
    fs.unlink(pathname);
  }
}

export interface ResignIpaOptions {
  ipaBytes: Uint8Array;
  certificate: Uint8Array;
  privateKey: Uint8Array;
  provisioningProfile: Uint8Array;
  password?: string;
  bundleId?: string;
  displayName?: string;
  bundleVersion?: string;
  adhoc?: boolean;
  forceSign?: boolean;
  zipLevel?: number;
  onLog?: (msg: string) => void;
}

export async function resignIpaInBrowser(options: ResignIpaOptions): Promise<Uint8Array> {
  const log = options.onLog ?? (() => undefined);
  log('sign: inizializzazione motore zsign WebAssembly...');
  const bundle = await loadZsignBundle();
  const moduleFactory = bundle.createEmbeddedZsignModule || bundle.createZsignModule;
  if (typeof moduleFactory !== 'function') {
    throw new Error('Factory zsign WASM non disponibile');
  }

  const client = await bundle.ZsignWasmClient.create({
    moduleFactory,
    moduleOptions: {},
  });
  const fs = client.mod.FS;
  if (!fs) {
    throw new Error('Filesystem virtuale Emscripten non disponibile');
  }

  log('sign: estrazione archivio IPA in memoria...');
  const inputZip = await JSZip.loadAsync(options.ipaBytes);
  const workspace = `/zsign_ws_${Date.now()}_${Math.floor(Math.random() * 1e9)}`;
  const inputRoot = `${workspace}/input`;
  const assetRoot = `${workspace}/assets`;
  fs.mkdirTree(inputRoot);
  fs.mkdirTree(assetRoot);

  try {
    for (const name of Object.keys(inputZip.files)) {
      const entry = inputZip.files[name];
      const cleanName = normalizeZipPath(name);
      if (!cleanName) continue;
      const outPath = `${inputRoot}/${cleanName}`;
      if (entry.dir) {
        fs.mkdirTree(outPath);
      } else {
        const data = await entry.async('uint8array');
        writeFsFile(fs, outPath, data);
      }
    }

    const certPath = `${assetRoot}/cert.bin`;
    const pkeyPath = `${assetRoot}/pkey.bin`;
    const provPath = `${assetRoot}/prov.mobileprovision`;
    writeFsFile(fs, certPath, options.certificate);
    writeFsFile(fs, pkeyPath, options.privateKey);
    writeFsFile(fs, provPath, options.provisioningProfile);

    log('sign: applicazione firma digitale Mach-O e provisioning...');
    client.signBundle(inputRoot, {
      certFile: certPath,
      pkeyFile: pkeyPath,
      provFile: provPath,
      password: options.password || '',
      bundleId: options.bundleId || '',
      bundleVersion: options.bundleVersion || '',
      displayName: options.displayName || '',
      adhoc: Boolean(options.adhoc),
      forceSign: options.forceSign !== undefined ? options.forceSign : true,
    });

    log('sign: ricompressione pacchetto IPA firmato...');
    const outZip = new JSZip();
    walkFsFiles(fs, inputRoot, (absPath) => {
      const relPath = absPath.slice(inputRoot.length + 1);
      const fileData = fs.readFile(absPath, { encoding: 'binary' });
      outZip.file(relPath, fileData);
    });

    return await outZip.generateAsync({
      type: 'uint8array',
      compression: 'DEFLATE',
      compressionOptions: {
        level: options.zipLevel ?? 6,
      },
    });
  } finally {
    rmrfFs(fs, workspace);
  }
}

export interface ParsedPkcs12Credentials {
  certificatePem: Uint8Array;
  privateKeyPem: Uint8Array;
  commonName?: string;
}

/**
 * Extracts PEM certificate(s) + private key from a standard `.p12` / `.pfx` file
 * completely client-side in browser RAM using `node-forge` (100% offline, zero network).
 */
export function parsePkcs12Credentials(
  p12Bytes: Uint8Array,
  password: string
): ParsedPkcs12Credentials {
  let binary = '';
  const chunkSize = 0x8000;
  for (let i = 0; i < p12Bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...p12Bytes.subarray(i, i + chunkSize));
  }

  const asn1 = forge.asn1.fromDer(binary);
  const p12 = forge.pkcs12.pkcs12FromAsn1(asn1, false, password);

  const certBags = p12.getBags({ bagType: forge.pki.oids.certBag })[
    forge.pki.oids.certBag
  ] ?? [];
  const shroudedKeyBags = p12.getBags({
    bagType: forge.pki.oids.pkcs8ShroudedKeyBag,
  })[forge.pki.oids.pkcs8ShroudedKeyBag] ?? [];
  const plainKeyBags = p12.getBags({ bagType: forge.pki.oids.keyBag })[
    forge.pki.oids.keyBag
  ] ?? [];

  const allKeyBags = [...shroudedKeyBags, ...plainKeyBags];
  const keyBag = allKeyBags.find((b) => Boolean(b.key));
  if (!keyBag?.key) {
    throw new Error('Nessuna chiave privata trovata nel file .p12 (verifica la password)');
  }

  const certs = certBags.map((b) => b.cert).filter((c): c is forge.pki.Certificate => Boolean(c));
  if (certs.length === 0) {
    throw new Error('Nessun certificato trovato nel file .p12');
  }

  // Put leaf certificate first (matching private key modulus if RSA), followed by chain
  const certsPem = certs.map((c) => forge.pki.certificateToPem(c)).join('\n');
  const keyPem = forge.pki.privateKeyToPem(keyBag.key);
  const cnAttr = certs[0].subject.getField('CN');
  const commonName = typeof cnAttr?.value === 'string' ? cnAttr.value : undefined;

  const encoder = new TextEncoder();
  return {
    certificatePem: encoder.encode(certsPem),
    privateKeyPem: encoder.encode(keyPem),
    commonName,
  };
}
