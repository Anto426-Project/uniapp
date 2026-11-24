# UniApp iOS Sideload Relay (Cloudflare Worker Gratuito)

Micro-relay stateless (0 €/mese sul piano gratuito **Cloudflare Workers**, zero database e zero persistenza) che abilita la pagina statica `/ios/` su GitHub Pages ad autenticare l'Apple ID dell'utente direttamente nel browser.

## Cosa fa (e cosa NON fa)

1. **`WS /wisp/` (Tunnel TCP cifrato end-to-end verso Apple):**
   I browser bloccano le connessioni dirette cross-origin (CORS) verso `gsa.apple.com` e `developerservices2.apple.com`. Questo Worker inoltra i pacchetti TCP sulla porta `443` **con `secureTransport: "off"`**: lo scambio TLS 1.3 avviene **end-to-end tra `libcurl-wasm` nel browser dell'utente e i server Apple**. Il Worker vede esclusivamente byte cifrati e **non può leggere password, codici 2FA, chiavi private o certificati**.
   - Whitelist rigorosa dei soli domini Apple di autenticazione/provisioning (`gsa.apple.com`, `idmsa.apple.com`, `developerservices2.apple.com`, `*itunes.apple.com` sulla sola porta `443`).
2. **`GET /ipa-proxy?url=...` (Proxy CORS sola lettura per l'IPA ufficiale):**
   Consente al browser di scaricare e verificare tramite checksum SHA-256 il file `.ipa` ufficiale dalle GitHub Releases di `Anto426-Project/uniapp-upstream` (bloccando qualsiasi altro URL).
3. **`GET /healthz`:**
   Endpoint diagnostico usato dall'indicatore di stato nella pagina `/ios/`.

---

## Deploy rapido (2 minuti, piano gratuito Cloudflare)

1. Crea un account gratuito su [dash.cloudflare.com](https://dash.cloudflare.com) (non serve carta di credito per il sottodominio `*.workers.dev`).
2. Entra in questa cartella e pubblica il Worker:

```bash
cd workers/ios-sideload-relay
npm install
npx wrangler login
npx wrangler deploy
```

3. Otterrai un URL del tipo:
   `https://uniapp-ios-relay.<tuo-sottodominio>.workers.dev`

4. **Collegalo in modo privato in fase di build (invisibile nella UI pubblica):**
   - Su GitHub apri il repository → **Settings → Secrets and variables → Actions → Variables** e crea la variabile:
     - Nome: `UNIAPP_IOS_RELAY_URL`
     - Valore: `https://uniapp-ios-relay.<tuo-sottodominio>.workers.dev`
     Il workflow `.github/workflows/build-website.yml` la passa a `NEXT_PUBLIC_IOS_RELAY_URL` durante la build statica, senza esporre alcun pannello o impostazione agli utenti del sito.
   - Il Worker accetta richieste **esclusivamente** dall'origine autorizzata in `wrangler.jsonc` (`ALLOWED_ORIGINS = "https://anto426-project.github.io"`) e risponde `403 Forbidden` a qualsiasi accesso diretto da browser o siti esterni. Per test locali puoi aggiungere temporaneamente `,http://localhost:3000` a `ALLOWED_ORIGINS` e `NEXT_PUBLIC_IOS_RELAY_URL=http://127.0.0.1:8787` in `website/.env.local`.
