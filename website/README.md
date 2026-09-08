# Sito UniApp

Il sito usa Next.js con esportazione statica. GitHub Pages pubblica `docs/` del repository `uniapp-upstream`, sotto `/uniapp-upstream/`.

## Sviluppo e verifica

```sh
npm ci
npm run dev
```

Prima di pubblicare:

```sh
npm run build:gh-pages
npx playwright install chromium
npm run test:e2e
```

`check:export` verifica che gli asset referenziati nell'HTML esistano e abbiano il percorso corretto. I test del browser controllano il carosello a 320 px, su mobile e desktop, inclusi trascinamento, scorrimento verticale, riduzione del movimento, anteprima e download ARM64.

Per visualizzare l'export in locale:

```sh
NEXT_PUBLIC_BASE_PATH=/uniapp-upstream npm start
```

Aprire `http://127.0.0.1:4173/uniapp-upstream/`. Il server locale serve solo l'export; GitHub Pages non richiede un server Node.

## Struttura

- `src/app/page.tsx` compone le sezioni statiche della pagina.
- `SiteShell` gestisce tema e manifest aggiornamenti. Le sezioni statiche vengono passate come contenuto, senza trasferire la loro logica al browser.
- `ScreenshotCarousel` gestisce navigazione e trascinamento con Embla. Le trasformazioni visive sono in un foglio CSS dedicato.
- `ScreenshotsGallery` collega la scheda selezionata alla finestra di anteprima.
- `scripts/prepare-screenshots.mjs` genera anteprime WebP larghe 480 pixel prima della build; gli originali sono riservati alla finestra di anteprima. Le immagini generate non si modificano a mano.
- `public/update.json` viene riempito dal workflow con il manifest della release verificata prima della build. Il repository sorgente mantiene un oggetto vuoto per evitare versioni o link di download inventati.

Le build Android e iOS partono manualmente. Il workflow di pubblicazione può seguire una build Android manuale riuscita; verifica il sito prima di pubblicarne i file.
