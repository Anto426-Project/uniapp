import {
  DirectUsbMuxClient,
  LOCKDOWN_PORT,
  WebUsbTransport,
  createHostId,
  createOpenSslWasmTlsFactory,
  createSystemBuid,
  decodeStoredPairRecord,
  encodeStoredPairRecord,
  generatePairRecordWithOpenSslWasm,
  installIpaViaInstProxy,
  sanitizeIpaFileName,
  type PairRecord,
  type StoredPairRecordPayload,
} from './webmuxd/core';

const HOST_ID_KEY = 'uniapp:ios-host-id';
const SYSTEM_BUID_KEY = 'uniapp:ios-system-buid';
const PAIR_RECORDS_KEY = 'uniapp:ios-pair-records';

export interface PairedIosDevice {
  udid: string;
  name: string | null;
}

type WasmPairRecordPayload = Pick<
  PairRecord,
  | 'hostId'
  | 'systemBuid'
  | 'hostCertificatePem'
  | 'hostPrivateKeyPem'
  | 'rootCertificatePem'
  | 'rootPrivateKeyPem'
  | 'deviceCertificatePem'
>;

function normalizePem(value: string): string {
  const trimmed = value.replace(/\r\n/g, '\n').trim();
  return trimmed.endsWith('\n') ? trimmed : `${trimmed}\n`;
}

export function isWebUsbSupported(): boolean {
  return typeof window !== 'undefined' && 'usb' in window.navigator;
}

export function isPairingDialogPendingError(error: unknown): boolean {
  const msg = error instanceof Error ? error.message : String(error);
  return msg.includes('PairingDialogResponsePending');
}

export function isUsbClaimInterfaceError(error: unknown): boolean {
  const msg = (error instanceof Error ? error.message : String(error)).toLowerCase();
  return (
    msg.includes('claiminterface') ||
    msg.includes('unable to claim interface') ||
    msg.includes('access denied') ||
    msg.includes('securityerror')
  );
}

function getOrCreateHostId(): string {
  try {
    const existing = window.localStorage.getItem(HOST_ID_KEY);
    if (existing && existing.trim()) return existing;
    const created = createHostId();
    window.localStorage.setItem(HOST_ID_KEY, created);
    return created;
  } catch {
    return createHostId();
  }
}

function getOrCreateSystemBuid(): string {
  try {
    const existing = window.localStorage.getItem(SYSTEM_BUID_KEY);
    if (existing && existing.trim()) return existing;
    const created = createSystemBuid();
    window.localStorage.setItem(SYSTEM_BUID_KEY, created);
    return created;
  } catch {
    return createSystemBuid();
  }
}

function loadPairRecordMap(): Record<string, StoredPairRecordPayload> {
  try {
    const raw = window.localStorage.getItem(PAIR_RECORDS_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as unknown;
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return {};
    return parsed as Record<string, StoredPairRecordPayload>;
  } catch {
    return {};
  }
}

function loadPairRecordForUdid(udid: string): PairRecord | null {
  const map = loadPairRecordMap();
  const stored = map[udid.trim()];
  if (!stored) return null;
  try {
    return decodeStoredPairRecord(stored);
  } catch {
    return null;
  }
}

function savePairRecordForUdid(udid: string, record: PairRecord): void {
  const key = udid.trim();
  if (!key) return;
  try {
    const map = loadPairRecordMap();
    map[key] = encodeStoredPairRecord(record);
    window.localStorage.setItem(PAIR_RECORDS_KEY, JSON.stringify(map));
  } catch {
    // Optional persistence
  }
}

export function listKnownPairedUdids(): string[] {
  if (typeof window === 'undefined') return [];
  return Object.keys(loadPairRecordMap());
}

export interface PairIosDeviceOptions {
  clientRef: { current: DirectUsbMuxClient | null };
  onLog: (msg: string) => void;
  onTrustPromptRequired?: () => void;
  onDisconnected?: () => void;
}

export async function pairIosDeviceOverWebUsb(
  options: PairIosDeviceOptions
): Promise<PairedIosDevice> {
  if (!isWebUsbSupported()) {
    throw new Error(
      'WebUSB non è supportato da questo browser. Usa Google Chrome, Microsoft Edge o Brave da computer.'
    );
  }

  let client = options.clientRef.current;
  if (!client) {
    options.onLog('usb: selezione dispositivo iOS via WebUSB...');
    const transport = await WebUsbTransport.requestAppleDevice();
    transport.setDisconnectHandler(() => {
      options.clientRef.current = null;
      options.onDisconnected?.();
    });

    client = new DirectUsbMuxClient(transport, {
      log: options.onLog,
      lockdownLabel: 'uniapp.web.sideload',
      tlsFactory: createOpenSslWasmTlsFactory(),
      pairRecordFactory: {
        createPairRecord: async (req) => {
          const payloadText = await generatePairRecordWithOpenSslWasm({
            devicePublicKey: req.devicePublicKey,
            hostId: req.hostId,
            systemBuid: req.systemBuid,
          });
          const payload = JSON.parse(payloadText) as WasmPairRecordPayload;
          return {
            hostId: payload.hostId,
            systemBuid: payload.systemBuid,
            hostCertificatePem: normalizePem(payload.hostCertificatePem),
            hostPrivateKeyPem: normalizePem(payload.hostPrivateKeyPem),
            rootCertificatePem: normalizePem(payload.rootCertificatePem),
            rootPrivateKeyPem: normalizePem(payload.rootPrivateKeyPem),
            deviceCertificatePem: normalizePem(payload.deviceCertificatePem),
            devicePublicKey: new Uint8Array(req.devicePublicKey),
          };
        },
      },
    });
    options.clientRef.current = client;
  }

  try {
    if (!client.isHandshakeComplete) {
      options.onLog('usb: apertura canale usbmux...');
      await client.openAndHandshake();
    }
    if (!client.isLockdownConnected) {
      options.onLog('usb: connessione a lockdownd...');
      await client.connectLockdown(LOCKDOWN_PORT);
    }

    const udid = await client.getOrFetchDeviceUdid();
    const name = await client.getOrFetchDeviceName();

    let hostId = getOrCreateHostId();
    let systemBuid = getOrCreateSystemBuid();

    const storedPair = loadPairRecordForUdid(udid);
    if (storedPair && !client.isPaired) {
      client.loadPairRecord(storedPair);
      hostId = storedPair.hostId;
      systemBuid = storedPair.systemBuid;
      try {
        window.localStorage.setItem(HOST_ID_KEY, hostId);
        window.localStorage.setItem(SYSTEM_BUID_KEY, systemBuid);
      } catch {
        // Optional persistence
      }
      options.onLog(`usb: record di pairing locale ripristinato per ${udid}`);
    }

    if (!client.isPaired) {
      options.onLog('usb: creazione pairing sicuro con il dispositivo...');
      try {
        const pairResult = await client.pairDevice(hostId, systemBuid);
        savePairRecordForUdid(udid, pairResult);
        options.onLog('usb: dispositivo autorizzato con successo');
      } catch (err) {
        if (isPairingDialogPendingError(err)) {
          options.onTrustPromptRequired?.();
        }
        throw err;
      }
    }

    if (!client.isSessionStarted) {
      const session = await client.startSession(hostId, systemBuid);
      options.onLog(`usb: sessione attiva (TLS=${String(session.enableSessionSsl)})`);
    }

    return { udid, name };
  } catch (err) {
    if (!isPairingDialogPendingError(err)) {
      try {
        await client.close();
      } catch {
        // Ignore cleanup errors
      }
      options.clientRef.current = null;
    }
    throw err;
  }
}

export async function installSignedIpaOverWebUsb(options: {
  client: DirectUsbMuxClient;
  signedFile: File;
  onLog: (msg: string) => void;
  onProgress?: (percent: number, stage: string) => void;
}): Promise<void> {
  const bytes = new Uint8Array(await options.signedFile.arrayBuffer());
  const safeName = sanitizeIpaFileName(options.signedFile.name);

  options.onProgress?.(5, 'Trasferimento IPA via AFC...');
  await installIpaViaInstProxy(options.client, bytes, safeName, (msg) => {
    options.onLog(msg);
    const pctMatch = msg.match(/(\d{1,3})%/);
    if (pctMatch && options.onProgress) {
      const rawPct = Math.min(100, Math.max(0, Number.parseInt(pctMatch[1], 10)));
      if (msg.toLowerCase().includes('afc') || msg.toLowerCase().includes('upload')) {
        options.onProgress(Math.round(5 + rawPct * 0.5), 'Trasferimento pacchetto via USB...');
      } else {
        options.onProgress(Math.round(55 + rawPct * 0.45), 'Installazione su iOS in corso...');
      }
    }
  });
  options.onProgress?.(100, 'Installazione completata');
}
