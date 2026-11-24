import type { PlatformDownload, PlatformManifest } from '@/data/types';
import { toIpaProxyUrl } from './relay-config';

export interface OfficialIosArtifact {
  version: string;
  tag: string;
  releaseUrl: string;
  ipaDownload: PlatformDownload | null;
  simulatorDownload: PlatformDownload | null;
}

export function resolveOfficialIosArtifact(
  platforms: PlatformManifest
): OfficialIosArtifact | null {
  const ios = platforms.platforms.ios;
  if (!ios) return null;

  const downloads = Object.values(ios.variants || {}).flatMap((v) => v.downloads || []);
  const ipaDownload = downloads.find((d) => d.name.toLowerCase().endsWith('.ipa')) ?? null;
  const simulatorDownload =
    downloads.find((d) => !d.name.toLowerCase().endsWith('.ipa')) ?? null;

  return {
    version: ios.version,
    tag: ios.tag,
    releaseUrl: ios.url,
    ipaDownload,
    simulatorDownload,
  };
}

export function buildSideStoreInstallUrl(ipaUrl: string): string {
  return `sidestore://install?url=${encodeURIComponent(ipaUrl)}`;
}

export function buildAltStoreInstallUrl(ipaUrl: string): string {
  return `altstore://install?url=${encodeURIComponent(ipaUrl)}`;
}

export function buildSideStoreSourceUrl(sourceJsonUrl: string): string {
  return `sidestore://source?url=${encodeURIComponent(sourceJsonUrl)}`;
}

export function buildAltStoreSourceUrl(sourceJsonUrl: string): string {
  return `altstore://source?url=${encodeURIComponent(sourceJsonUrl)}`;
}

async function computeSha256Hex(bytes: Uint8Array): Promise<string> {
  const copy = new Uint8Array(bytes.byteLength);
  copy.set(bytes);
  const digest = await crypto.subtle.digest('SHA-256', copy.buffer);
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
}

export async function downloadOfficialIpaVerified(options: {
  artifact: PlatformDownload;
  relayBaseUrl?: string;
  onProgress?: (percent: number) => void;
  onLog?: (msg: string) => void;
}): Promise<File> {
  const log = options.onLog ?? (() => undefined);
  const proxyUrl = toIpaProxyUrl(options.artifact.url, options.relayBaseUrl);
  const targetUrl = proxyUrl || options.artifact.url;

  log(`download: scaricamento ${options.artifact.name}...`);
  let response: Response;
  try {
    response = await fetch(targetUrl, { cache: 'no-store' });
  } catch {
    throw new Error(
      'Impossibile scaricare automaticamente l’IPA: scaricalo dal pulsante "IPA diretto" e trascinalo nel riquadro qui sotto.'
    );
  }

  if (!response.ok) {
    throw new Error(`Download IPA fallito (HTTP ${response.status})`);
  }

  const contentLength = Number(response.headers.get('content-length') || 0);
  let bytes: Uint8Array;

  if (response.body && contentLength > 0) {
    const reader = response.body.getReader();
    const chunks: Uint8Array[] = [];
    let received = 0;

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      if (value) {
        chunks.push(value);
        received += value.byteLength;
        options.onProgress?.(Math.min(99, Math.round((received / contentLength) * 100)));
      }
    }

    bytes = new Uint8Array(received);
    let offset = 0;
    for (const chunk of chunks) {
      bytes.set(chunk, offset);
      offset += chunk.byteLength;
    }
  } else {
    bytes = new Uint8Array(await response.arrayBuffer());
  }

  const expectedHash = (options.artifact.sha256 || '').trim().toLowerCase();
  if (expectedHash && /^[0-9a-f]{64}$/.test(expectedHash)) {
    log('download: verifica integrità crittografica SHA-256...');
    const actualHash = await computeSha256Hex(bytes);
    if (actualHash !== expectedHash) {
      throw new Error(
        `Verifica SHA-256 fallita per ${options.artifact.name}: atteso ${expectedHash.slice(0, 12)}…, ottenuto ${actualHash.slice(0, 12)}…`
      );
    }
    log(`download: checksum SHA-256 verificato (${actualHash.slice(0, 12)}…)`);
  }

  options.onProgress?.(100);
  const outBuf = new Uint8Array(bytes.byteLength);
  outBuf.set(bytes);
  return new File([outBuf.buffer], options.artifact.name, {
    type: 'application/octet-stream',
  });
}
