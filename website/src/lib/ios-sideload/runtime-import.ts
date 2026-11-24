import { withBasePath } from '@/utils/basePath';

// Keep native browser import() intact at runtime so Next.js/Turbopack never parses
// heavy Emscripten/WASM static bundles stored under public/ios-sideload/.
const nativeDynamicImport = new Function('specifier', 'return import(specifier)') as <T>(
  specifier: string
) => Promise<T>;

export async function runtimeImportPublic<T>(publicSubpath: string): Promise<T> {
  return nativeDynamicImport<T>(withBasePath(publicSubpath));
}
