import { UpdateManifest, ScreenshotItem } from './types';

// No invented version or stale download when the distribution manifest is unavailable.
export const DEFAULT_MANIFEST: UpdateManifest = {};

export const SCREENSHOTS_BY_THEME: Record<string, ScreenshotItem[]> = {
  violet: [
    {
      file: "violet-home.jpg",
      title: "Dashboard & Carriera",
      description: "Panoramica esami, media e stato studente"
    },
    {
      file: "violet-servizi.jpg",
      title: "Servizi Universitari",
      description: "Navette, trasporti, tasse e segreteria"
    },
    {
      file: "violet-didattica.jpg",
      title: "Didattica & Libretto",
      description: "Libretto universitario, esami e piano di studi"
    },
    {
      file: "violet-info.jpg",
      title: "Informazioni UniApp",
      description: "Dettagli build, note di rilascio e aggiornamenti"
    },
    {
      file: "violet-badge.jpg",
      title: "Badge Universitario",
      description: "QR Code studente e tessera digitale"
    },
    {
      file: "violet-statistiche.jpg",
      title: "Statistiche & Andamento",
      description: "Grafici andamento voti, CFU e proiezioni carriera"
    }
  ],
  sapphire: [
    {
      file: "sapphire-home.jpg",
      title: "Dashboard & Carriera",
      description: "Panoramica esami, media e stato studente"
    },
    {
      file: "sapphire-servizi.jpg",
      title: "Servizi Universitari",
      description: "Navette, trasporti, tasse e segreteria"
    },
    {
      file: "sapphire-didattica.jpg",
      title: "Didattica & Libretto",
      description: "Libretto universitario, esami e piano di studi"
    },
    {
      file: "sapphire-info.jpg",
      title: "Informazioni UniApp",
      description: "Dettagli build, note di rilascio e aggiornamenti"
    },
    {
      file: "sapphire-badge.jpg",
      title: "Badge Universitario",
      description: "QR Code studente e tessera digitale"
    },
    {
      file: "sapphire-statistiche.jpg",
      title: "Statistiche & Andamento",
      description: "Grafici andamento voti, CFU e proiezioni carriera"
    }
  ],
  emerald: [
    {
      file: "emerald-home.jpg",
      title: "Dashboard & Carriera",
      description: "Panoramica esami, media e stato studente"
    },
    {
      file: "emerald-servizi.jpg",
      title: "Servizi Universitari",
      description: "Navette, trasporti, tasse e segreteria"
    },
    {
      file: "emerald-didattica.jpg",
      title: "Didattica & Libretto",
      description: "Libretto universitario, esami e piano di studi"
    },
    {
      file: "emerald-info.jpg",
      title: "Informazioni UniApp",
      description: "Dettagli build, note di rilascio e aggiornamenti"
    },
    {
      file: "emerald-badge.jpg",
      title: "Badge Universitario",
      description: "QR Code studente e tessera digitale"
    },
    {
      file: "emerald-statistiche.jpg",
      title: "Statistiche & Andamento",
      description: "Grafici andamento voti, CFU e proiezioni carriera"
    }
  ],
  amber: [
    {
      file: "amber-home.jpg",
      title: "Dashboard & Carriera",
      description: "Panoramica esami, media e stato studente"
    },
    {
      file: "amber-servizi.jpg",
      title: "Servizi Universitari",
      description: "Navette, trasporti, tasse e segreteria"
    },
    {
      file: "amber-didattica.jpg",
      title: "Didattica & Libretto",
      description: "Libretto universitario, esami e piano di studi"
    },
    {
      file: "amber-info.jpg",
      title: "Informazioni UniApp",
      description: "Dettagli build, note di rilascio e aggiornamenti"
    },
    {
      file: "amber-badge.jpg",
      title: "Badge Universitario",
      description: "QR Code studente e tessera digitale"
    },
    {
      file: "amber-statistiche.jpg",
      title: "Statistiche & Andamento",
      description: "Grafici andamento voti, CFU e proiezioni carriera"
    }
  ]
};

export const SCREENSHOTS_DATA: ScreenshotItem[] = SCREENSHOTS_BY_THEME.violet;
