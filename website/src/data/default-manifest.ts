import { UpdateManifest, ScreenshotItem } from './types';

export const DEFAULT_MANIFEST: UpdateManifest = {
  channels: {
    stable: {
      release: {
        latestVersion: "1.5.2",
        minSupportedVersion: "1.1.0",
        mandatory: true,
        downloadUrl: "https://raw.githubusercontent.com/Anto426-Project/UniappUpstream/main/src/release/androidApp-release.apk",
        notes: "Bug fixes e ottimizzazioni di stabilità generale.",
        publishedAt: "2026-03-08",
        appEnabled: true,
        description: "Versione stabile e verificata di UniApp per gli studenti dell'Università degli Studi del Molise."
      }
    },
    beta: {
      release: {
        latestVersion: "1.8.9-beta",
        latestVersionCode: 199,
        minSupportedVersion: "1.8.5-beta",
        minSupportedVersionCode: 195,
        mandatory: true,
        releaseChannel: "beta",
        downloadUrl: "https://github.com/Anto426-Project/UniappUpstream/releases/download/v1.8.9-beta%2B199/androidApp-universal-release.apk",
        downloadUrlsByAbi: {
          "arm64-v8a": "https://github.com/Anto426-Project/UniappUpstream/releases/download/v1.8.9-beta%2B199/androidApp-arm64-v8a-release.apk",
          "universal": "https://github.com/Anto426-Project/UniappUpstream/releases/download/v1.8.9-beta%2B199/androidApp-universal-release.apk",
          "armeabi-v7a": "https://github.com/Anto426-Project/UniappUpstream/releases/download/v1.8.9-beta%2B199/androidApp-armeabi-v7a-release.apk"
        },
        notes: "Changelog 25 May 2026:\n- Aggiunta la compilazione dei questionari con supporto a piu' pagine, domande obbligatorie e domande opzionali.\n- Gestiti piu' questionari per materia e collegata la nuova schermata di compilazione dalla lista questionari.\n- Corretto il QR studente usando il contenuto badge corretto e una generazione piu' vicina al badge ufficiale.\n- Aggiunto il pulsante per annullare gli appelli prenotati quando la prenotazione e' cancellabile.\n- Migliorate prenotazioni e cancellazioni dei trasporti, inclusa la selezione dei soli giorni feriali.\n- Uniformato il padding inferiore delle schermate di dettaglio e rifiniti toast e componenti comuni.",
        publishedAt: "2026-05-25",
        buildCommit: "d8634232cf263241918d8a236de58262b3a3ff67",
        appEnabled: true,
        description: "Stanchi della vecchia app universitaria?\nScopri UniApp, l’app non ufficiale per gli studenti dell’Università degli Studi del Molise, sviluppata in autonomia da Anto426. Completamente riscritta in Kotlin nativo, utilizza le più recenti tecnologie Material 3 Design e Jetpack Compose per offrire un’esperienza moderna, fluida e intuitiva. Gestisci la tua carriera, consulta il libretto, prenota gli esami e accedi rapidamente alle informazioni più importanti, tutto in un’unica interfaccia veloce e curata."
      }
    }
  }
};

export const SCREENSHOTS_DATA: ScreenshotItem[] = [
  {
    file: "Screenshot_2026-04-03-23-22-14-44_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Dashboard & Carriera",
    description: "Panoramica esami, media e stato studente"
  },
  {
    file: "Screenshot_2026-04-03-23-22-22-02_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Libretto & Base Laurea",
    description: "Consultazione voti e calcolo proiezioni"
  },
  {
    file: "Screenshot_2026-04-03-23-22-27-00_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Appelli d'Esame",
    description: "Prenotazione e cancellazione esami"
  },
  {
    file: "Screenshot_2026-04-03-23-22-34-61_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Tasse & Contributi",
    description: "Stato pagamenti e avvisi PagoPA"
  },
  {
    file: "Screenshot_2026-04-03-23-22-45-38_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Badge Universitario",
    description: "QR Code studente e tessera digitale"
  },
  {
    file: "Screenshot_2026-04-03-23-22-53-05_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Questionari ANVUR",
    description: "Valutazione didattica a schede"
  },
  {
    file: "Screenshot_2026-04-03-23-22-59-32_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Servizi Navetta",
    description: "Orari e prenotazione trasporti nei feriali"
  },
  {
    file: "Screenshot_2026-04-03-23-23-02-77_92b74ce0392afcb9dbc7c9e6841482f8.jpg",
    title: "Personalizzazione Temi",
    description: "Design Liquid Monet dinamico"
  }
];
