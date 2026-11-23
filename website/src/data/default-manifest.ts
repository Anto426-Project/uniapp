import { UpdateManifest, ScreenshotItem } from './types';

// No invented version or stale download when the distribution manifest is unavailable.
export const DEFAULT_MANIFEST: UpdateManifest = {};

function buildThemeScreenshots(prefix: string): ScreenshotItem[] {
  return [
    {
      file: `${prefix}-home.jpg`,
      title: 'Dashboard & Carriera',
      description: 'Panoramica esami, media, CFU, appelli, tasse e accesso rapido',
    },
    {
      file: `${prefix}-servizi.jpg`,
      title: 'Servizi Universitari',
      description: 'Trasporti, tasse, rubrica, notizie e portali ufficiali di Ateneo',
    },
    {
      file: `${prefix}-didattica.jpg`,
      title: 'Didattica & Carriera',
      description: 'Media ponderata, progresso CFU, libretto, appelli e presenze QR',
    },
    {
      file: `${prefix}-badge.jpg`,
      title: 'Badge Universitario',
      description: 'Tessera studente interattiva 3D con QR Code nativo',
    },
    {
      file: `${prefix}-impostazioni.jpg`,
      title: 'Impostazioni & Sicurezza',
      description: 'Dispositivi connessi, biometria, Tema Studio, notifiche e 10 lingue',
    },
    {
      file: `${prefix}-aggiornamenti.jpg`,
      title: 'Aggiornamenti Sistema',
      description: 'Verifica pacchetti, canale stabile e note di rilascio integrate',
    },
    {
      file: `${prefix}-info.jpg`,
      title: 'Informazioni UniApp',
      description: 'Versione build, progetto open source, crediti e SDK integrati',
    },
  ];
}

// Exact order matching ThemeViewModel in UniApp: Zaffiro, Smeraldo, Tramonto, Viola.
export const SCREENSHOTS_BY_THEME: Record<string, ScreenshotItem[]> = {
  sapphire: buildThemeScreenshots('sapphire'),
  emerald: buildThemeScreenshots('emerald'),
  sunset: buildThemeScreenshots('sunset'),
  violet: buildThemeScreenshots('violet'),
};

export const SCREENSHOTS_DATA: ScreenshotItem[] = SCREENSHOTS_BY_THEME.sapphire;
