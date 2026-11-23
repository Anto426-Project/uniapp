import type { AppearanceMode, MonetThemeId, ScreenshotItem } from './types';
import availableFiles from './available-screenshots.json';

export interface MonetThemeMeta {
  id: MonetThemeId;
  label: string;
  swatchHex: string;
  assetPrefix: string;
}

// Exact order matching ThemeViewModel in UniApp: Zaffiro, Smeraldo, Tramonto, Viola.
export const MONET_THEMES: readonly MonetThemeMeta[] = [
  { id: 'sapphire', label: 'Zaffiro', swatchHex: '#4A90D9', assetPrefix: 'sapphire' },
  { id: 'emerald', label: 'Smeraldo', swatchHex: '#2ECC71', assetPrefix: 'emerald' },
  { id: 'sunset', label: 'Tramonto', swatchHex: '#E67E22', assetPrefix: 'sunset' },
  { id: 'violet', label: 'Viola', swatchHex: '#9B59B6', assetPrefix: 'violet' },
] as const;

const AVAILABLE_SET = new Set<string>(availableFiles as string[]);

interface ScreenDef {
  id: string;
  title: string;
  description: string;
  optional?: boolean;
}

const SCREEN_DEFS: readonly ScreenDef[] = [
  {
    id: 'home',
    title: 'Dashboard',
    description: 'Media ponderata, base di laurea, CFU, scadenze imminenti, accesso rapido personalizzabile e notizie.',
  },
  {
    id: 'servizi',
    title: 'Servizi e portali',
    description: 'Navette, tasse, rubrica, avvisi e collegamenti diretti a Esse3, Moodle, Segreteria ed Email.',
  },
  {
    id: 'didattica',
    title: 'Didattica e simulatore',
    description: 'Avanzamento carriera, libretto, piano di studi, scenari sui CFU rimanenti e presenze aula.',
  },
  {
    id: 'badge',
    title: 'Badge UniPass 3D',
    description: 'Tessera studente interattiva fronte-retro con QR accademico generato nativamente sul dispositivo.',
  },
  {
    id: 'impostazioni',
    title: 'Impostazioni e profili',
    description: 'Cambio rapido tra più account, Dispositivi connessi, sblocco biometrico, Tema Studio e 10 lingue.',
  },
  {
    id: 'aggiornamenti',
    title: 'Updater integrato',
    description: 'Canale stabile, note di rilascio integrate e verifica automatica della firma dopo il download.',
  },
  {
    id: 'info',
    title: 'Progetto open source',
    description: 'Versione build, crediti, accesso al codice su GitHub e versioni degli SDK integrati.',
  },
  {
    id: 'crediti',
    title: 'Creatore e crediti',
    description: 'Profilo GitHub del creatore, statistiche del progetto e riconoscimenti ai contributori della community.',
    optional: true,
  },
];

function hasScreenshotVariant(theme: MonetThemeId, screenId: string): boolean {
  return (
    AVAILABLE_SET.has(`${theme}-${screenId}.jpg`) ||
    AVAILABLE_SET.has(`${theme}-light-${screenId}.jpg`)
  );
}

function resolveScreenshotFile(theme: MonetThemeId, appearance: AppearanceMode, screenId: string): string {
  const lightFile = `${theme}-light-${screenId}.jpg`;
  const darkFile = `${theme}-${screenId}.jpg`;
  if (appearance === 'light') {
    if (AVAILABLE_SET.has(lightFile)) return lightFile;
    return darkFile;
  }
  if (AVAILABLE_SET.has(darkFile)) return darkFile;
  if (AVAILABLE_SET.has(lightFile)) return lightFile;
  return darkFile;
}

export function getScreenshots(theme: MonetThemeId, appearance: AppearanceMode = 'dark'): ScreenshotItem[] {
  return SCREEN_DEFS.filter((screen) => !screen.optional || hasScreenshotVariant(theme, screen.id)).map(
    (screen) => ({
      id: screen.id,
      file: resolveScreenshotFile(theme, appearance, screen.id),
      title: screen.title,
      description: screen.description,
    })
  );
}

export const SCREENSHOTS_BY_THEME: Record<MonetThemeId, ScreenshotItem[]> = {
  sapphire: getScreenshots('sapphire', 'dark'),
  emerald: getScreenshots('emerald', 'dark'),
  sunset: getScreenshots('sunset', 'dark'),
  violet: getScreenshots('violet', 'dark'),
};

export function previewPathFor(file: string): string {
  return `/assets/screenshots/previews/${file.replace(/\.[^.]+$/, '.webp')}`;
}

export function fullPathFor(file: string): string {
  return `/assets/screenshots/${file}`;
}
