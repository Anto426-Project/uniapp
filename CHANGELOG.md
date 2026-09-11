## [2.0.4] - 2026-09-11 (build set by CI)

### UI e funzionalità App
- Semplificata la conferma password nelle impostazioni dell'app con il pulsante "Conferma".
- Riorganizzato il banner informativo dell'app con versione, build e accesso rapido alle note di rilascio.
- Introdotto il nuovo selettore rapido degli account con card profilo dedicata e gestione avatar.
- Riprogettati i banner di aggiornamento con hero header coerente e feedback di avanzamento.
- Armonizzato lo stile grafico delle card notizie nella schermata principale con il design Liquid.

### CI / Build infrastruttura
- Il `versionCode` Android usa `uniapp.versionCodeBase + GITHUB_RUN_NUMBER` (base attuale: 1000), eliminando il bump manuale e mantenendo la progressione rispetto alle vecchie build.
- I 4 SDK pubblicano AAR Android ottimizzati con R8, metadati KMP e KLIB iOS su **GitHub Packages** e come archivio Maven nelle **GitHub Releases**, ad ogni push sul rispettivo branch principale, con versione `1.0.<run_number>`.
- UniApp scarica le Release SDK con autenticazione anche per gli allegati privati, verifica gli hash e risolve le versioni esatte da un repository Maven locale. Versioni e revisioni vengono conservate con l'artefatto della build. Rimossi i submodule e i composite build degli SDK.
- Le classi offuscate di ogni SDK usano uno spazio di nomi distinto per evitare collisioni nell'APK; il resolver verifica anche l'assenza di classi duplicate prima di aggiornare la cache.
- Eliminato il workflow ridondante `build-shared-libs.yml`; le build Android e iOS restano manuali e riusano i binari SDK già compilati.
- `publish-build.yml`: aggiunto controllo di progressione del `versionCode` — rifiuta la pubblicazione se il codice dell'APK in arrivo è ≤ all'ultimo pubblicato.
- `bump_version.py`: non modifica più il `versionCode` nel gradle (ora gestito dal CI); aggiorna solo `versionName` e `update-config.json`.

### Refactor data layer
- Introdotto `UniAppDataCoordinator` e il sistema `UniAppDataRequest`/`UniAppDataSnapshot`: pipeline reattiva strutturata che sostituisce i blocchi `coroutineScope + async` sparsi nei ViewModel.
- `UniAppCachePolicies` estratto in file dedicato; il controllo dell'età della cache corretto in `0 until` (limite superiore esclusivo).
- Aggiunto flag `fallbackToStaleCache` su `SessionUniAppDataSource` — disabilitato per i coordinator UI per evitare che dati corrotti o scaduti vengano serviti silenziosamente.
- `AccountAvatarStore` e `ApplicationImageStore` introdotti per la condivisione dei ritratti tra account e il caching cross-account delle immagini.
- `UniLocalDataStore`: locking a granularità per-chiave al posto di un unico mutex globale; aggiunto `invalidateAccount` per la pulizia della sessione.
- `ProjectDataStore` estratto; chiave cache `GitHubProject` aggiunta a `UniAppDataKeys`.
- Tutti i ViewModel migrati al pattern `observe` tramite `UniAppDataCoordinator`.
- `AppSessionController`: pulizia dello scope lifecycle alla chiusura della sessione.
- Aggiunti test unitari completi: `AccountAvatarStoreTest`, `ApplicationImageStoreTest`, `ApplicationStorageTest`, `UniAppDataCoordinatorTest`, `StorageTestFactory`.

## [2.0.2] - 2026-09-08 (build 202)

- Corretto il crash della home quando non sono disponibili notizie.
- Ripristinati i test Android prima della pubblicazione.
