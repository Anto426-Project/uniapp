export const IOS_RELAY_URL = normalizeRelayBaseUrl(
  process.env.NEXT_PUBLIC_IOS_RELAY_URL || ''
);

export const DEFAULT_IOS_RELAY_URL = IOS_RELAY_URL;

export function normalizeRelayBaseUrl(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed) return '';
  try {
    const withScheme =
      trimmed.startsWith('http://') ||
      trimmed.startsWith('https://') ||
      trimmed.startsWith('ws://') ||
      trimmed.startsWith('wss://')
        ? trimmed
        : `https://${trimmed}`;
    const parsed = new URL(withScheme);
    if (parsed.protocol === 'wss:') parsed.protocol = 'https:';
    if (parsed.protocol === 'ws:') parsed.protocol = 'http:';
    const cleanPath = parsed.pathname
      .replace(/\/wisp\/?$/i, '')
      .replace(/\/healthz\/?$/i, '')
      .replace(/\/+$/, '');
    return `${parsed.origin}${cleanPath}`;
  } catch {
    return '';
  }
}

export function toWispWebSocketUrl(relayBaseUrl: string = IOS_RELAY_URL): string {
  const base = normalizeRelayBaseUrl(relayBaseUrl);
  if (!base) return '';
  const parsed = new URL(base);
  parsed.protocol = parsed.protocol === 'http:' ? 'ws:' : 'wss:';
  parsed.pathname = `${parsed.pathname.replace(/\/+$/, '')}/wisp/`;
  return parsed.toString();
}

export function toIpaProxyUrl(
  targetIpaUrl: string,
  relayBaseUrl: string = IOS_RELAY_URL
): string {
  const base = normalizeRelayBaseUrl(relayBaseUrl);
  if (!base) return '';
  const parsed = new URL(`${base.replace(/\/+$/, '')}/ipa-proxy`);
  parsed.searchParams.set('url', targetIpaUrl);
  return parsed.toString();
}
