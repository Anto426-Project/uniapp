# UniApp — Scripts

Tutti gli script del repository sono organizzati per dominio funzionale e coperti da CI/test. Eseguirli sempre dalla root del progetto.

## Struttura

### `sdk/` — Risoluzione e verifica binari SDK
- `fetch_sdk_binaries.py` — Scarica dalle GitHub Releases le versioni esatte dichiarate in `gradle/libs.versions.toml`, verifica SHA-256, manifest licenze e unicità classi Android, e installa atomicamente in `.sdk-binaries/maven`.
- `sdk_binaries.json` — Mappa coordinate Maven ↔ repository GitHub dei quattro SDK precompilati.

```sh
python3 scripts/sdk/fetch_sdk_binaries.py
```

### `checks/` — Validazioni statiche e gate CI
- `validate_release.py` — Entrypoint CI: esegue tutti i test in `scripts/tests/` più `check_app_strings.py` e `check_motion_ownership.py`.
- `check_app_strings.py` — Verifica validità XML, duplicati e riferimenti `Res.string.*` nei sorgenti Kotlin.
- `check_motion_ownership.py` — Verifica che le animazioni restino centralizzate in Liquid Monet e `AppNavigationHost`.
- `android_page_sizes.py` — Controlla allineamento ELF/RELRO/ZIP a 16 KB delle librerie `.so` in APK e AAR.

```sh
python3 scripts/checks/validate_release.py
python3 scripts/checks/android_page_sizes.py androidApp/build/outputs/apk/release/*.apk
```

### `release/` — Versioning, packaging e pubblicazione GitHub
- `bump_version.py` — Aggiorna `versionName` (`androidApp/build.gradle.kts`, `iosApp/Configuration/Config.xcconfig`), `update-config.json` e `CHANGELOG.md` (richiamabile su Windows anche via `bump-version.bat`).
- `release_metadata.py` — Helper condivisi per validazione APK, hash SHA-256 e URL release.
- `prepare_build_artifact.py` — Impacchetta gli output Android o iOS con `context.json` verificato da checksum SHA-256.
- `collect_build_artifacts.py` — Valida gli artefatti scaricati in CI e genera `plan.json` + `SHA256SUMS.txt`.
- `publish_builds.py` — Orchestra pubblicazione release GitHub e aggiornamento indice `builds.json` / `platforms.json`.
- `publish_android.sh` — Pubblica il canale release Android firmato e aggiorna `update.json` sul repo di distribuzione.
- `publish_website.sh` — Pubblica `website/out` nella cartella `docs/` del repository di distribuzione senza sovrascrivere i manifest app.
- `generate_update_manifest.py` — Genera `update.json` da metadati APK verificati e `update-config.json`.
- `generate_github_release_metadata.py` — Genera note di rilascio Markdown e variabili `GITHUB_ENV`.
- `generate_deploy_readme.py` — Genera il `README.md` del repository di distribuzione.
- `resolve_publish_source.cjs` — Modulo Node per GitHub Actions che seleziona solo build `main` completate con successo.

### `media/` — Cattura screenshot dal telefono
- `capture-phone-screenshots.mjs` — Naviga automaticamente UniApp sul telefono Android collegato via ADB/UIAutomator (con login automatico sull'account demo `demo@uniapp.local` se l'app è disconnessa) per catturare tutti i 4 temi (`sapphire`, `emerald`, `sunset`, `violet`) in entrambi gli aspetti (`dark` e `light`) sulle 8 schermate del sito (`home`, `servizi`, `didattica`, `badge`, `impostazioni`, `aggiornamenti`/`update`, `info`, `crediti` con scorrimento verso il basso — 64 screenshot totali). Taglia automaticamente la barra notifiche superiore (`statusBars`, es. `160px` su `1440×3168`) direttamente sugli screenshot salvati e rigenera anteprime WebP e indice `available-screenshots.json`.

```sh
node scripts/media/capture-phone-screenshots.mjs --check
node scripts/media/capture-phone-screenshots.mjs
node scripts/media/capture-phone-screenshots.mjs --crop-existing
node scripts/media/capture-phone-screenshots.mjs --appearance light
node scripts/media/capture-phone-screenshots.mjs --screen info,update,crediti
node scripts/media/capture-phone-screenshots.mjs --theme sapphire --appearance dark --screen crediti
node scripts/media/capture-phone-screenshots.mjs --manual
```

### `tests/` — Test automatici degli script
- `test_*.py` — Suite `unittest` per SDK, release, metadati, allineamento 16 KB, motion ownership, Xcode build phase e publish shell.
- `resolve_publish_source.test.cjs` — Suite `node:test` per `scripts/release/resolve_publish_source.cjs`.

```sh
python3 scripts/checks/validate_release.py
node --test scripts/tests/resolve_publish_source.test.cjs
```
