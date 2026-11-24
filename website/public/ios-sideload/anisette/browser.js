import ModuleFactory from "./anisette_rs.js";
export { Anisette } from "./anisette.js";
export { WasmBridge } from "./wasm-bridge.js";
export { Device } from "./device.js";
export { LibraryStore } from "./library.js";
export { ProvisioningSession } from "./provisioning.js";
export { FetchHttpClient } from "./http.js";

const wasmUrl = new URL("./anisette_rs.wasm", import.meta.url).href;
export async function loadWasmModule(moduleOverrides = {}) {
  return ModuleFactory({
    ...moduleOverrides,
    locateFile: (filename) => filename.endsWith(".wasm") ? wasmUrl : filename,
  });
}
export const loadWasm = loadWasmModule;
export const isNodeEnvironment = () => false;
export const getWasmBinaryPath = () => wasmUrl;
