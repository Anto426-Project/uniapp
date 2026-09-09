## [2.0.4] - 2026-09-09 (build set by CI)

### CI / Build infrastruttura
- Il `versionCode` Android è ora impostato automaticamente dal numero di run del workflow CI (`GITHUB_RUN_NUMBER`), eliminando il bump manuale dell'intero dal gradle.
- I 4 SDK (`liquid-monet`, `uni-sdk`, `secure-storage-sdk`, `firebase-connector-sdk`) pubblicano ora gli AAR pre-compilati con R8 full-mode su **GitHub Packages** ad ogni push su `main`, con versione Maven `1.0.<run_number>`.
- `uniapp` risolve i SDK come dipendenze Maven pre-compilate in CI (versione `1.0.+`); in locale l'`includeBuild` continua a usare i sorgenti dei submodule per lo sviluppo iterativo.
- Nuovo workflow **`build-shared-libs.yml`**: pre-compila `composeApp` e tutti i moduli SDK con R8 ad ogni push, riscaldando la Gradle cache del runner self-hosted per i build successivi.
- `build-android.yml` e `build-ios.yml`: i submodule non vengono più clonati in CI (si usano i binari di GitHub Packages); Gradle cache impostata in read-only.
- `build-ios.yml`: aggiunta cache `actions/cache` per il framework Kotlin/Native (chiave basata sull'hash dei sorgenti) per evitare ricompilazioni su runner macOS effimeri.
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
- Ripristinati i test Android e i controlli sul sito prima della pubblicazione.
- Carosello mobile ottimizzato, anteprime più leggere e un solo download ARM64.
