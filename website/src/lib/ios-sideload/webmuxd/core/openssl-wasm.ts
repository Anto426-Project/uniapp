import { withBasePath } from '@/utils/basePath';
import { runtimeImportPublic } from '../../runtime-import';
import type { TlsConnection, TlsConnectionFactory } from './imobiledevice-client';

export interface OpenSslWasmConnectionRequest {
  serverName: string;
  caCertificatePem: string;
  certificatePem: string;
  privateKeyPem: string;
}

export interface OpenSslWasmPairRecordRequest {
  devicePublicKey: Uint8Array;
  hostId: string;
  systemBuid: string;
}

interface OpenSslWasmModule {
  default(input?: unknown): Promise<unknown>;
  opensslClientConstructor: new (
    serverName: string,
    caCertificatePem: string,
    certificatePem: string,
    privateKeyPem: string
  ) => TlsConnection;
  generatePairRecord(devicePublicKey: Uint8Array, hostId: string, systemBuid: string): string;
}

let opensslWasmModule: OpenSslWasmModule | null = null;
let opensslWasmModulePromise: Promise<OpenSslWasmModule> | null = null;
let opensslWasmInitPromise: Promise<void> | null = null;

const toOpenSslWasmModule = (moduleValue: unknown): OpenSslWasmModule => {
  if (!moduleValue || typeof moduleValue !== 'object') {
    throw new Error('OpenSSL wasm module did not return an object');
  }

  const candidate = moduleValue as Record<string, unknown>;
  if (typeof candidate.default !== 'function') {
    throw new Error('OpenSSL wasm module is missing its default initializer');
  }
  if (typeof candidate.OpensslClient !== 'function') {
    throw new Error('OpenSSL wasm module is missing OpensslClient');
  }
  if (typeof candidate.libimobiledevice_generate_pair_record !== 'function') {
    throw new Error('OpenSSL wasm module is missing pair record generation');
  }

  return {
    default: candidate.default as OpenSslWasmModule['default'],
    opensslClientConstructor:
      candidate.OpensslClient as OpenSslWasmModule['opensslClientConstructor'],
    generatePairRecord:
      candidate.libimobiledevice_generate_pair_record as OpenSslWasmModule['generatePairRecord'],
  };
};

const loadOpenSslWasmModule = async (): Promise<OpenSslWasmModule> => {
  if (!opensslWasmModulePromise) {
    opensslWasmModulePromise = runtimeImportPublic<unknown>(
      '/ios-sideload/openssl/openssl_wasm.js'
    ).then((moduleValue) => {
      const loadedModule = toOpenSslWasmModule(moduleValue);
      opensslWasmModule = loadedModule;
      return loadedModule;
    });
  }

  return await opensslWasmModulePromise;
};

const requireOpenSslWasmModule = (): OpenSslWasmModule => {
  if (!opensslWasmModule) {
    throw new Error('OpenSSL wasm is not ready. Call ensureOpenSslWasmReady() first.');
  }

  return opensslWasmModule;
};

export const ensureOpenSslWasmReady = async (): Promise<void> => {
  if (!opensslWasmInitPromise) {
    opensslWasmInitPromise = loadOpenSslWasmModule()
      .then(async (moduleValue) => {
        await moduleValue.default({
          module_or_path: withBasePath('/ios-sideload/openssl/openssl_wasm_bg.wasm'),
        });
      })
      .catch((err) => {
        opensslWasmInitPromise = null;
        throw err;
      });
  }

  await opensslWasmInitPromise;
};

export const createOpenSslWasmConnection = (
  request: OpenSslWasmConnectionRequest
): TlsConnection => {
  const moduleValue = requireOpenSslWasmModule();
  return new moduleValue.opensslClientConstructor(
    request.serverName,
    request.caCertificatePem,
    request.certificatePem,
    request.privateKeyPem
  );
};

export const createOpenSslWasmTlsFactory = (): TlsConnectionFactory => {
  return {
    ensureReady: ensureOpenSslWasmReady,
    createConnection: createOpenSslWasmConnection,
  };
};

export const generatePairRecordWithOpenSslWasm = async (
  request: OpenSslWasmPairRecordRequest
): Promise<string> => {
  await ensureOpenSslWasmReady();
  const moduleValue = requireOpenSslWasmModule();
  return moduleValue.generatePairRecord(
    new Uint8Array(request.devicePublicKey),
    request.hostId,
    request.systemBuid
  );
};
