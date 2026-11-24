import { strFromU8, unzipSync } from 'fflate';
import { runtimeImportPublic } from './runtime-import';
import { getAnisetteData, initLibcurl, requireLibcurl, type AnisetteData } from './anisette-client';
import { parsePkcs12Credentials, resignIpaInBrowser } from './zsign-resigner';

const SIGNING_IDENTITY_STORAGE_KEY = 'uniapp:ios-signing-identities';
const PRIMARY_APP_INFO_PLIST_RE = /^Payload\/[^/]+\.app\/Info\.plist$/;
const GSA_TRUSTED_DEVICE_URL = 'https://gsa.apple.com/auth/verify/trusteddevice';
const GSA_VALIDATE_URL = 'https://gsa.apple.com/grandslam/GsService2/validate';
const GSA_PHONE_URL = 'https://gsa.apple.com/auth/verify/phone';
const GSA_PHONE_CODE_URL = 'https://gsa.apple.com/auth/verify/phone/securitycode';
const REAUTH_SENTINEL = '__UNIAPP_NEEDS_FULL_REAUTH__';

export interface AppleTeam {
  name: string;
  identifier: string;
  type?: string;
}

export interface AppleCertificate {
  name: string;
  identifier: string;
  serialNumber?: string;
  machineName?: string;
  publicKey: Uint8Array;
}

export interface AppleDevice {
  name: string;
  identifier: string;
}

export interface AppleAppId {
  name: string;
  identifier: string;
  bundleIdentifier: string;
}

export interface AppleDeveloperSession {
  anisetteData: AnisetteData;
  dsid: string;
  authToken: string;
}

export interface AppleDeveloperContext {
  appleId: string;
  session: AppleDeveloperSession;
  team: AppleTeam;
  certificates: AppleCertificate[];
  devices: AppleDevice[];
}

export interface TrustedPhoneNumber {
  id: number;
  numberWithDialCode: string;
  obfuscatedNumber: string;
  pushMode: string;
}

export interface TwoFactorContext {
  submitDeviceCode: (code: string) => void;
  trustedPhoneNumbers: TrustedPhoneNumber[];
  requestSms: (phoneId: number) => Promise<void>;
  submitSmsCode: (phoneId: number, code: string) => Promise<void>;
}

interface AppleApiInstance {
  authenticate(
    appleId: string,
    password: string,
    anisetteData: AnisetteData,
    verificationHandler?: (submitCode: (code: string) => void) => void
  ): Promise<{ session: AppleDeveloperSession }>;
  fetchTeam(session: AppleDeveloperSession): Promise<AppleTeam>;
  fetchCertificates(session: AppleDeveloperSession, team: AppleTeam): Promise<AppleCertificate[]>;
  fetchDevices(session: AppleDeveloperSession, team: AppleTeam): Promise<AppleDevice[]>;
  registerDevice(
    session: AppleDeveloperSession,
    team: AppleTeam,
    name: string,
    udid: string
  ): Promise<AppleDevice>;
  fetchAppIDs(session: AppleDeveloperSession, team: AppleTeam): Promise<AppleAppId[]>;
  addAppID(
    session: AppleDeveloperSession,
    team: AppleTeam,
    name: string,
    bundleId: string
  ): Promise<AppleAppId>;
  addCertificate(
    session: AppleDeveloperSession,
    team: AppleTeam,
    machineName: string
  ): Promise<{ certificate: AppleCertificate; privateKey: Uint8Array }>;
  revokeCertificate(
    session: AppleDeveloperSession,
    team: AppleTeam,
    cert: AppleCertificate
  ): Promise<void>;
  fetchProvisioningProfile(
    session: AppleDeveloperSession,
    team: AppleTeam,
    appId: AppleAppId
  ): Promise<{ data: Uint8Array }>;
}

type AltsignFetch = {
  get(url: string, headers?: Record<string, string>): Promise<{ text(): Promise<string>; ok: boolean }>;
  request(
    method: string,
    url: string,
    headers: Record<string, string>,
    body?: string
  ): Promise<{ text(): Promise<string>; ok: boolean }>;
};

interface AltsignApiModule {
  AppleAPI: new (fetch: unknown) => AppleApiInstance;
  Fetch: new (
    initFn: () => Promise<unknown>,
    fetcher: (
      url: string,
      options: { method?: string; headers?: HeadersInit; body?: BodyInit | null }
    ) => Promise<Response>
  ) => AltsignFetch;
}

interface CachedSigningIdentityPayload {
  certId: string;
  certPublicKeyBase64: string;
  privateKeyBase64: string;
}

let altsignModPromise: Promise<AltsignApiModule> | null = null;
let appleFetchInstance: AltsignFetch | null = null;

async function loadAltsignApiModule(): Promise<AltsignApiModule> {
  if (!altsignModPromise) {
    altsignModPromise = runtimeImportPublic<AltsignApiModule>(
      '/ios-sideload/altsign/apple-api.mjs'
    ).catch((err) => {
      altsignModPromise = null;
      throw err;
    });
  }
  return altsignModPromise;
}

function patchHandleTwoFactor(api: AppleApiInstance, fetchClient: AltsignFetch): void {
  const auth = (api as unknown as { auth?: Record<string, unknown> }).auth;
  if (!auth || typeof auth.handleTwoFactor !== 'function') return;

  auth.handleTwoFactor = async function (
    dsid: string,
    idmsToken: string,
    anisetteData: AnisetteData,
    verificationHandler: ((ctx: TwoFactorContext) => void) | undefined
  ): Promise<boolean> {
    if (!verificationHandler) return false;
    try {
      const formatDate = auth.formatDate as (d: Date) => string;
      const identityToken = btoa(`${dsid}:${idmsToken}`);
      const headers: Record<string, string> = {
        'Content-Type': 'text/x-xml-plist',
        'User-Agent': 'Xcode',
        Accept: 'text/x-xml-plist',
        'Accept-Language': 'it-it,en-us',
        'X-Apple-App-Info': 'com.apple.gs.xcode.auth',
        'X-Xcode-Version': '15.2 (15C500b)',
        'X-Apple-Identity-Token': identityToken,
        'X-Apple-I-MD-M': anisetteData.machineID,
        'X-Apple-I-MD': anisetteData.oneTimePassword,
        'X-Apple-I-MD-LU': anisetteData.localUserID,
        'X-Apple-I-MD-RINFO': String(anisetteData.routingInfo),
        'X-Mme-Device-Id': anisetteData.deviceUniqueIdentifier,
        'X-MMe-Client-Info': anisetteData.deviceDescription,
        'X-Apple-I-Client-Time': formatDate.call(auth, anisetteData.date),
        'X-Apple-Locale': anisetteData.locale,
        'X-Apple-I-TimeZone': anisetteData.timeZone,
      };

      let trustedPhoneNumbers: TrustedPhoneNumber[] = [];
      try {
        const deviceResp = await fetchClient.get(GSA_TRUSTED_DEVICE_URL, headers);
        const deviceText = await deviceResp.text();
        trustedPhoneNumbers = parseTrustedPhones(deviceText);
      } catch {
        // Trusted device notification attempted; SMS list optional
      }

      return await new Promise<boolean>((resolve) => {
        const jsonHeaders = {
          ...headers,
          'Content-Type': 'application/json',
          Accept: 'application/json',
        };

        const submitDeviceCode = async (code: string) => {
          const trimmed = code.trim();
          if (!trimmed || trimmed === '__CANCELLED__') {
            resolve(false);
            return;
          }
          try {
            const verifyHeaders = { ...headers, 'security-code': trimmed };
            const resp = await fetchClient.get(GSA_VALIDATE_URL, verifyHeaders);
            const text = await resp.text();
            const plist = parsePlistSimple(text);
            resolve((plist as Record<string, unknown>)['ec'] === 0);
          } catch {
            resolve(false);
          }
        };

        const requestSms = async (phoneId: number): Promise<void> => {
          const body = JSON.stringify({ phoneNumber: { id: phoneId }, mode: 'sms' });
          const resp = await fetchClient.request('PUT', GSA_PHONE_URL, jsonHeaders, body);
          const respText = await resp.text();
          if (!resp.ok) {
            throw new Error(`Invio SMS fallito (${respText.slice(0, 100)})`);
          }
        };

        const submitSmsCode = async (phoneId: number, code: string): Promise<void> => {
          const body = JSON.stringify({
            phoneNumber: { id: phoneId },
            securityCode: { code: code.trim() },
            mode: 'sms',
          });
          const resp = await fetchClient.request('POST', GSA_PHONE_CODE_URL, jsonHeaders, body);
          const text = await resp.text();
          let ok = resp.ok;
          try {
            const parsed = JSON.parse(text) as Record<string, unknown>;
            if (parsed['ec'] !== undefined) ok = parsed['ec'] === 0;
          } catch {
            // Non-JSON body
          }
          if (!ok) throw new Error('Codice SMS non valido o scaduto');
          resolve(true);
        };

        try {
          verificationHandler({
            submitDeviceCode,
            trustedPhoneNumbers,
            requestSms,
            submitSmsCode,
          });
        } catch {
          resolve(false);
        }
      });
    } catch {
      return false;
    }
  };
}

function patchAuthenticateRetry(api: AppleApiInstance): void {
  const auth = (api as unknown as { auth?: Record<string, unknown> }).auth;
  if (!auth || typeof auth.authenticate !== 'function') return;

  let storedVerificationHandler: ((ctx: TwoFactorContext) => void) | undefined;
  let twoFactorDone = false;

  if (typeof auth.sendAuthRequest === 'function') {
    const origSend = (
      auth.sendAuthRequest as (...a: unknown[]) => Promise<Record<string, unknown>>
    ).bind(auth);

    auth.sendAuthRequest = async function (...args: unknown[]): Promise<Record<string, unknown>> {
      const params = args[0] as Record<string, unknown>;
      const anisetteArg = args[1] as AnisetteData;

      let response: Record<string, unknown>;
      try {
        response = await origSend(...args);
      } catch (e) {
        const isApptokens = params?.['o'] === 'apptokens';
        const is22421 = e instanceof Error && e.message.includes('(-22421)');
        if (isApptokens && is22421 && !twoFactorDone) {
          response = {};
        } else {
          throw e;
        }
      }

      const status = response['Status'] as Record<string, unknown> | undefined;
      if (status?.['au'] === 'secondaryAuth') {
        status['au'] = 'trustedDeviceSecondaryAuth';
        return response;
      }

      if (params?.['o'] === 'apptokens' && Object.keys(response).length === 0) {
        if (twoFactorDone) {
          throw new Error('Autenticazione non riuscita dopo la verifica a due fattori. Riprova.');
        }
        const adsid = params['u'] as string;
        const idmsToken = params['t'] as string;
        const handleTwoFactor = auth.handleTwoFactor as (
          d: string,
          t: string,
          a: AnisetteData,
          h: ((ctx: TwoFactorContext) => void) | undefined
        ) => Promise<boolean>;

        if (adsid && idmsToken && storedVerificationHandler) {
          const success = await handleTwoFactor.call(
            auth,
            adsid,
            idmsToken,
            anisetteArg,
            storedVerificationHandler
          );
          if (success) {
            twoFactorDone = true;
            throw new Error(REAUTH_SENTINEL);
          }
        }
        throw new Error('Verifica a due fattori annullata.');
      }

      return response;
    };
  }

  const original = (auth.authenticate as (...args: unknown[]) => Promise<unknown>).bind(auth);
  let callCount = 0;

  auth.authenticate = async function (...args: unknown[]): Promise<unknown> {
    callCount += 1;
    storedVerificationHandler = args[3] as ((ctx: TwoFactorContext) => void) | undefined;
    if (callCount > 1) {
      try {
        const freshAnisette = await getAnisetteData();
        args = [...args];
        args[2] = freshAnisette;
      } catch {
        // Proceed with current anisette if refresh fails
      }
    }
    return await original(...args);
  };
}

async function getAppleApi(
  onLog?: (msg: string) => void
): Promise<{ api: AppleApiInstance; fetch: AltsignFetch }> {
  const uiLog = onLog ?? (() => undefined);
  const { AppleAPI, Fetch } = await loadAltsignApiModule();

  if (!appleFetchInstance) {
    const doFetch = async (
      url: string,
      options: { method?: string; headers?: HeadersInit; body?: BodyInit | null }
    ): Promise<Response> => {
      const curl = requireLibcurl();
      const libcurlOpts = {
        method: options.method,
        headers: options.headers,
        body: options.body,
        redirect: 'manual',
        insecure: true,
        _libcurl_http_version: 1.1,
      };
      let response = await curl.fetch(url, libcurlOpts);

      if (url.includes('gsa.apple.com') && response.status === 503) {
        const delays = [4000, 8000, 16000];
        for (let attempt = 0; attempt < delays.length; attempt += 1) {
          uiLog(
            `login: server Apple occupato (503), nuovo tentativo tra ${delays[attempt] / 1000}s...`
          );
          await new Promise((r) => setTimeout(r, delays[attempt]));
          response = await curl.fetch(url, libcurlOpts);
          if (response.status !== 503) break;
        }
        if (response.status === 503) {
          throw new Error(
            'I server di autenticazione Apple sono temporaneamente occupati (503). Riprova tra qualche minuto.'
          );
        }
      }
      return response;
    };

    appleFetchInstance = new Fetch(() => initLibcurl(), doFetch);
  }

  const api = new AppleAPI(appleFetchInstance);
  patchHandleTwoFactor(api, appleFetchInstance);
  patchAuthenticateRetry(api);
  return { api, fetch: appleFetchInstance };
}

export async function loginAppleDeveloper(options: {
  appleId: string;
  password: string;
  relayBaseUrl?: string;
  onLog?: (msg: string) => void;
  onTwoFactorRequired?: (ctx: TwoFactorContext) => void;
}): Promise<AppleDeveloperContext> {
  const appleId = options.appleId.trim();
  const password = options.password;
  if (!appleId || !password) {
    throw new Error('Inserisci Apple ID e password.');
  }

  const log = options.onLog ?? (() => undefined);
  const anisetteData = await getAnisetteData(log, options.relayBaseUrl);
  log(`login: autenticazione SRP con Apple ID (${maskEmail(appleId)})...`);

  appleFetchInstance = null;

  const verificationCallback = (submitCode: (code: string) => void) => {
    if (!options.onTwoFactorRequired) {
      throw new Error('Account protetto da 2FA ma nessun gestore di codice attivo.');
    }
    options.onTwoFactorRequired(submitCode as unknown as TwoFactorContext);
  };

  let { api } = await getAppleApi(log);
  let session: AppleDeveloperSession;

  try {
    ({ session } = await api.authenticate(appleId, password, anisetteData, verificationCallback));
  } catch (e) {
    if (e instanceof Error && e.message === REAUTH_SENTINEL) {
      log('login: verifica 2FA completata, riapertura sessione SRP...');
      appleFetchInstance = null;
      const fresh = await getAppleApi(log);
      api = fresh.api;
      const freshAnisette = await getAnisetteData(log, options.relayBaseUrl);
      try {
        ({ session } = await api.authenticate(
          appleId,
          password,
          freshAnisette,
          verificationCallback
        ));
      } catch (reauthErr) {
        if (reauthErr instanceof Error && reauthErr.message === REAUTH_SENTINEL) {
          throw new Error(
            'Autenticazione non completata dopo il codice 2FA. Esegui nuovamente il login.'
          );
        }
        throw reauthErr;
      }
    } else {
      throw e;
    }
  }

  log('login: recupero team di sviluppo, certificati e dispositivi registrati...');
  const team = await api.fetchTeam(session);
  const [certificates, devices] = await Promise.all([
    api.fetchCertificates(session, team),
    api.fetchDevices(session, team).catch(() => [] as AppleDevice[]),
  ]);

  log(`login: team attivo ${team.name} (${team.identifier})`);
  return {
    appleId,
    session,
    team,
    certificates,
    devices,
  };
}

export interface SignedIpaOutput {
  signedFile: File;
  outputBundleId: string;
  teamId?: string;
}

export async function signIpaWithAppleAccount(options: {
  ipaFile: File;
  context: AppleDeveloperContext;
  deviceUdid: string;
  deviceName?: string | null;
  bundleIdOverride?: string;
  displayNameOverride?: string;
  relayBaseUrl?: string;
  onLog?: (msg: string) => void;
}): Promise<{ result: SignedIpaOutput; updatedContext: AppleDeveloperContext }> {
  const log = options.onLog ?? (() => undefined);
  const freshAnisette = await getAnisetteData(log, options.relayBaseUrl);
  const session: AppleDeveloperSession = {
    ...options.context.session,
    anisetteData: freshAnisette,
  };

  const { api } = await getAppleApi(log);
  const team = await api.fetchTeam(session);
  const [certificates, devices] = await Promise.all([
    api.fetchCertificates(session, team),
    api.fetchDevices(session, team).catch(() => [] as AppleDevice[]),
  ]);

  const ipaBytes = new Uint8Array(await options.ipaFile.arrayBuffer());
  const ipaInfo = readIpaInfo(ipaBytes);
  const baseBundleId = (
    options.bundleIdOverride ||
    ipaInfo.bundleId ||
    'com.anto426.uniapp.UniApp'
  ).trim();
  const finalBundleId = buildTeamScopedBundleId(baseBundleId, team.identifier);
  const displayName = (options.displayNameOverride || ipaInfo.displayName || 'UniApp').trim();

  const identity = await ensureSigningIdentity(
    api,
    session,
    team,
    options.context.appleId,
    certificates,
    log
  );

  await ensureDeviceRegistered(
    api,
    session,
    team,
    devices,
    options.deviceUdid,
    options.deviceName ?? undefined,
    log
  );

  const appId = await ensureAppId(api, session, team, finalBundleId, log);
  log('sign: download profilo di provisioning (.mobileprovision)...');
  const profile = await api.fetchProvisioningProfile(session, team, appId);

  const signedBytes = await resignIpaInBrowser({
    ipaBytes,
    certificate: identity.certificate.publicKey,
    privateKey: identity.privateKey,
    provisioningProfile: profile.data,
    bundleId: finalBundleId,
    displayName: displayName || undefined,
    adhoc: false,
    forceSign: true,
    onLog: log,
  });

  const outBuf = new Uint8Array(signedBytes.byteLength);
  outBuf.set(signedBytes);
  const signedFile = new File([outBuf.buffer], toSignedFileName(options.ipaFile.name), {
    type: 'application/octet-stream',
  });

  log(`sign: completato (${Math.round(signedBytes.byteLength / 1024)} KB)`);
  return {
    result: {
      signedFile,
      outputBundleId: finalBundleId,
      teamId: team.identifier,
    },
    updatedContext: {
      appleId: options.context.appleId,
      session,
      team,
      certificates,
      devices,
    },
  };
}

/**
 * 100% offline local signing using user-supplied `.p12` + `.mobileprovision` (zero network).
 */
export async function signIpaWithOfflineP12(options: {
  ipaFile: File;
  p12File: File;
  p12Password: string;
  mobileProvisionFile: File;
  bundleIdOverride?: string;
  onLog?: (msg: string) => void;
}): Promise<SignedIpaOutput> {
  const log = options.onLog ?? (() => undefined);
  log('sign: lettura certificato .p12 e profilo .mobileprovision in locale...');
  const [ipaBuf, p12Buf, provBuf] = await Promise.all([
    options.ipaFile.arrayBuffer(),
    options.p12File.arrayBuffer(),
    options.mobileProvisionFile.arrayBuffer(),
  ]);

  const ipaBytes = new Uint8Array(ipaBuf);
  const p12Creds = parsePkcs12Credentials(new Uint8Array(p12Buf), options.p12Password);
  const provBytes = new Uint8Array(provBuf);
  const ipaInfo = readIpaInfo(ipaBytes);
  const bundleId = (options.bundleIdOverride || '').trim();

  const signedBytes = await resignIpaInBrowser({
    ipaBytes,
    certificate: p12Creds.certificatePem,
    privateKey: p12Creds.privateKeyPem,
    provisioningProfile: provBytes,
    bundleId: bundleId || undefined,
    adhoc: false,
    forceSign: true,
    onLog: log,
  });

  const outBuf = new Uint8Array(signedBytes.byteLength);
  outBuf.set(signedBytes);
  const signedFile = new File([outBuf.buffer], toSignedFileName(options.ipaFile.name), {
    type: 'application/octet-stream',
  });

  log(`sign: pacchetto firmato offline pronto (${Math.round(signedBytes.byteLength / 1024)} KB)`);
  return {
    signedFile,
    outputBundleId: bundleId || ipaInfo.bundleId || 'com.anto426.uniapp.UniApp',
  };
}

async function ensureSigningIdentity(
  api: AppleApiInstance,
  session: AppleDeveloperSession,
  team: AppleTeam,
  appleId: string,
  certificates: AppleCertificate[],
  onLog: (msg: string) => void
): Promise<{ certificate: AppleCertificate; privateKey: Uint8Array }> {
  const cached = loadCachedIdentity(appleId, team.identifier);
  if (cached) {
    const matched = certificates.find((item) => item.identifier === cached.certId);
    if (matched) {
      onLog(`sign: riutilizzo certificato di sviluppo locale (${matched.identifier})`);
      return {
        certificate: {
          ...matched,
          publicKey: base64ToBytes(cached.certPublicKeyBase64),
        },
        privateKey: base64ToBytes(cached.privateKeyBase64),
      };
    }
  }

  onLog('sign: emissione nuovo certificato di sviluppo Apple...');
  let created: { certificate: AppleCertificate; privateKey: Uint8Array };
  try {
    created = await api.addCertificate(session, team, `UniApp-Web-${Date.now()}`);
  } catch (err) {
    const msg = String(err);
    if (!msg.includes('7460') || certificates.length === 0) {
      throw err;
    }
    const target = certificates[0];
    onLog(`sign: limite certificati raggiunto (7460), revoca di ${target.identifier}...`);
    await api.revokeCertificate(session, team, target);
    created = await api.addCertificate(session, team, `UniApp-Web-${Date.now()}`);
  }

  saveCachedIdentity(appleId, team.identifier, {
    certId: created.certificate.identifier,
    certPublicKeyBase64: bytesToBase64(created.certificate.publicKey),
    privateKeyBase64: bytesToBase64(created.privateKey),
  });
  return created;
}

async function ensureDeviceRegistered(
  api: AppleApiInstance,
  session: AppleDeveloperSession,
  team: AppleTeam,
  devices: AppleDevice[],
  deviceUdid: string,
  deviceName: string | undefined,
  onLog: (msg: string) => void
): Promise<void> {
  const normalized = deviceUdid.trim().toUpperCase();
  if (!normalized) {
    throw new Error(
      'Inserisci o rileva via USB l’UDID del tuo iPhone/iPad per generare il profilo di provisioning.'
    );
  }
  const existing = devices.find((d) => d.identifier.trim().toUpperCase() === normalized);
  if (existing) {
    onLog(`sign: dispositivo già registrato sul team (${existing.name || normalized})`);
    return;
  }

  const label = deviceName?.trim() || `iPhone-${normalized.slice(-6)}`;
  onLog(`sign: registrazione UDID ${normalized} (${label}) sull’account Apple...`);
  await api.registerDevice(session, team, label, normalized);
}

async function ensureAppId(
  api: AppleApiInstance,
  session: AppleDeveloperSession,
  team: AppleTeam,
  bundleId: string,
  onLog: (msg: string) => void
): Promise<AppleAppId> {
  const appIds = await api.fetchAppIDs(session, team);
  const matched = appIds.find((item) => item.bundleIdentifier === bundleId);
  if (matched) {
    onLog(`sign: App ID esistente riutilizzato (${bundleId})`);
    return matched;
  }
  onLog(`sign: registrazione App ID ${bundleId}...`);
  return api.addAppID(session, team, 'UniApp', bundleId);
}

export function readIpaInfo(ipaBytes: Uint8Array): { bundleId?: string; displayName?: string } {
  try {
    const files = unzipSync(ipaBytes, {
      filter: (file) => PRIMARY_APP_INFO_PLIST_RE.test(file.name),
    });
    const infoName = Object.keys(files).find((name) => PRIMARY_APP_INFO_PLIST_RE.test(name));
    if (!infoName) return {};
    const parsed = parseInfoPlist(files[infoName]);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return {};
    const data = parsed as Record<string, unknown>;
    const bundleId = typeof data.CFBundleIdentifier === 'string' ? data.CFBundleIdentifier : undefined;
    const displayName =
      typeof data.CFBundleDisplayName === 'string'
        ? data.CFBundleDisplayName
        : typeof data.CFBundleName === 'string'
          ? data.CFBundleName
          : undefined;
    return { bundleId, displayName };
  } catch {
    return {};
  }
}

function parseInfoPlist(bytes: Uint8Array): unknown {
  if (bytes.length >= 8 && strFromU8(bytes.subarray(0, 8)) === 'bplist00') {
    return parseBinaryPlist(bytes);
  }
  return parseXmlPlist(strFromU8(bytes));
}

function parsePlistSimple(text: string): unknown {
  const trimmed = text.trim();
  if (trimmed.startsWith('{')) {
    try {
      return JSON.parse(trimmed);
    } catch {
      // Fall through
    }
  }
  return parseXmlPlist(trimmed);
}

function parseXmlPlist(xml: string): unknown {
  if (typeof DOMParser === 'undefined') return {};
  const doc = new DOMParser().parseFromString(xml, 'application/xml');
  if (doc.querySelector('parsererror')) return {};
  const root = doc.querySelector('plist > *');
  return root ? parseXmlNode(root) : {};
}

function parseXmlNode(node: Element): unknown {
  switch (node.tagName) {
    case 'dict': {
      const map: Record<string, unknown> = {};
      const children = Array.from(node.children);
      for (let i = 0; i < children.length - 1; i += 2) {
        if (children[i].tagName === 'key') {
          map[children[i].textContent ?? ''] = parseXmlNode(children[i + 1]);
        }
      }
      return map;
    }
    case 'array':
      return Array.from(node.children).map((c) => parseXmlNode(c));
    case 'integer':
      return Number.parseInt(node.textContent ?? '0', 10);
    case 'real':
      return Number.parseFloat(node.textContent ?? '0');
    case 'true':
      return true;
    case 'false':
      return false;
    case 'data':
      return base64ToBytes((node.textContent ?? '').trim());
    default:
      return node.textContent ?? '';
  }
}

function parseBinaryPlist(bytes: Uint8Array): unknown {
  if (bytes.length < 40) return {};
  const trailer = bytes.length - 32;
  const offsetSize = bytes[trailer + 6];
  const refSize = bytes[trailer + 7];
  const objectCount = readUInt(bytes, trailer + 8, 8);
  const topObject = readUInt(bytes, trailer + 16, 8);
  const offsetTable = readUInt(bytes, trailer + 24, 8);

  const offsets = new Array<number>(objectCount);
  for (let i = 0; i < objectCount; i += 1) {
    offsets[i] = readUInt(bytes, offsetTable + i * offsetSize, offsetSize);
  }

  const memo = new Map<number, unknown>();
  const readLen = (offset: number, info: number) => {
    if (info < 0x0f) return { length: info, next: offset + 1 };
    const intSize = 1 << (bytes[offset + 1] & 0x0f);
    return { length: readUInt(bytes, offset + 2, intSize), next: offset + 2 + intSize };
  };

  const parseObj = (idx: number): unknown => {
    if (memo.has(idx)) return memo.get(idx);
    const off = offsets[idx];
    const type = bytes[off] >> 4;
    const info = bytes[off] & 0x0f;
    let val: unknown = null;
    if (type === 0x0) val = info === 0x9;
    else if (type === 0x1) val = readUInt(bytes, off + 1, 1 << info);
    else if (type === 0x5) {
      const { length, next } = readLen(off, info);
      val = strFromU8(bytes.subarray(next, next + length));
    } else if (type === 0xa) {
      const { length, next } = readLen(off, info);
      val = Array.from({ length }, (_, i) => parseObj(readUInt(bytes, next + i * refSize, refSize)));
    } else if (type === 0xd) {
      const { length, next } = readLen(off, info);
      const valsOff = next + length * refSize;
      const dict: Record<string, unknown> = {};
      for (let i = 0; i < length; i += 1) {
        const k = parseObj(readUInt(bytes, next + i * refSize, refSize));
        if (typeof k === 'string') {
          dict[k] = parseObj(readUInt(bytes, valsOff + i * refSize, refSize));
        }
      }
      val = dict;
    }
    memo.set(idx, val);
    return val;
  };
  return parseObj(topObject);
}

function readUInt(bytes: Uint8Array, offset: number, length: number): number {
  let v = 0;
  for (let i = 0; i < length; i += 1) v = v * 256 + bytes[offset + i];
  return v;
}

function parseTrustedPhones(text: string): TrustedPhoneNumber[] {
  const trimmed = text.trim();
  const mapPhones = (raw: Record<string, unknown>[]): TrustedPhoneNumber[] =>
    raw.map((p) => ({
      id: typeof p['id'] === 'number' ? p['id'] : Number(p['id']),
      numberWithDialCode: typeof p['numberWithDialCode'] === 'string' ? p['numberWithDialCode'] : '',
      obfuscatedNumber: typeof p['obfuscatedNumber'] === 'string' ? p['obfuscatedNumber'] : '',
      pushMode: typeof p['pushMode'] === 'string' ? p['pushMode'] : 'sms',
    }));

  if (trimmed.startsWith('{')) {
    try {
      const parsed = JSON.parse(trimmed) as Record<string, unknown>;
      if (Array.isArray(parsed['trustedPhoneNumbers'])) {
        return mapPhones(parsed['trustedPhoneNumbers'] as Record<string, unknown>[]);
      }
    } catch {
      // Fall through
    }
  }

  const bootMatch = trimmed.match(/<script[^>]+class="boot_args"[^>]*>([\s\S]*?)<\/script>/i);
  if (bootMatch) {
    try {
      const boot = JSON.parse(bootMatch[1]) as Record<string, unknown>;
      const tdv = (boot['direct'] as Record<string, unknown> | undefined)?.[
        'trustedDeviceVerification'
      ] as Record<string, unknown> | undefined;
      const pnv = tdv?.['phoneNumberVerification'] as Record<string, unknown> | undefined;
      if (Array.isArray(pnv?.['trustedPhoneNumbers'])) {
        return mapPhones(pnv['trustedPhoneNumbers'] as Record<string, unknown>[]);
      }
    } catch {
      // Fall through
    }
  }
  return [];
}

function buildTeamScopedBundleId(baseBundleId: string, teamId: string): string {
  const b = baseBundleId.trim();
  const t = teamId.trim();
  if (!b || !t) return b;
  if (b.toLowerCase().endsWith(`.${t.toLowerCase()}`)) return b;
  return `${b}.${t}`;
}

function toSignedFileName(name: string): string {
  return name.toLowerCase().endsWith('.ipa')
    ? `${name.slice(0, -4)}-signed.ipa`
    : `${name}-signed.ipa`;
}

function loadCachedIdentity(appleId: string, teamId: string): CachedSigningIdentityPayload | null {
  try {
    const raw = window.localStorage.getItem(SIGNING_IDENTITY_STORAGE_KEY);
    if (!raw) return null;
    const map = JSON.parse(raw) as Record<string, CachedSigningIdentityPayload>;
    const key = `${appleId.trim().toLowerCase()}::${teamId.trim().toUpperCase()}`;
    const entry = map?.[key];
    return entry?.certId && entry.certPublicKeyBase64 && entry.privateKeyBase64 ? entry : null;
  } catch {
    return null;
  }
}

function saveCachedIdentity(
  appleId: string,
  teamId: string,
  payload: CachedSigningIdentityPayload
): void {
  try {
    const raw = window.localStorage.getItem(SIGNING_IDENTITY_STORAGE_KEY);
    const map = raw ? (JSON.parse(raw) as Record<string, CachedSigningIdentityPayload>) : {};
    const key = `${appleId.trim().toLowerCase()}::${teamId.trim().toUpperCase()}`;
    map[key] = payload;
    window.localStorage.setItem(SIGNING_IDENTITY_STORAGE_KEY, JSON.stringify(map));
  } catch {
    // Optional cache
  }
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = '';
  for (let i = 0; i < bytes.length; i += 1) binary += String.fromCharCode(bytes[i]);
  return btoa(binary);
}

function base64ToBytes(base64: string): Uint8Array {
  const clean = base64.replace(/\s+/g, '');
  const binary = atob(clean);
  const out = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) out[i] = binary.charCodeAt(i);
  return out;
}

function maskEmail(email: string): string {
  const at = email.indexOf('@');
  if (at <= 1) return '***';
  return `${email.slice(0, 2)}***${email.slice(at)}`;
}
