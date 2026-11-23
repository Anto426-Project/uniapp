# Sito UniApp

Sito ufficiale di distribuzione per UniApp, progettato seguendo i principi di **Apple Fluid Interfaces** (fisica a molla con `damping` e `response`, interruzione in volo, handoff della velocità, proiezione del momento, rubber-banding, materiali traslucidi con scroll edge effect, tipografia ottica di sistema e piena accessibilità).

## Comandi principali

```sh
npm ci
npm run dev
```

Verifica completa per GitHub Pages (`/uniapp-upstream/`):

```sh
npm run lint
npm run build:gh-pages
npm run test:e2e
```

Anteprima locale dell'export statico:

```sh
NEXT_PUBLIC_BASE_PATH=/uniapp-upstream npm start
```
