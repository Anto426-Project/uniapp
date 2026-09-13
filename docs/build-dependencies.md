# Dipendenze e runner

UniApp consuma esclusivamente binari Maven. Non usa composite build, sostituzioni
con repository vicini o versioni `latest`. Ciascuno dei quattro SDK compila e
pubblica autonomamente Android AAR, metadati KMP/Maven e KLIB iOS device/simulator.

## Aggiornare uno SDK

1. Modificare e verificare lo SDK nel suo repository.
2. Pubblicare una nuova versione tramite il workflow dello SDK. Il numero Maven
   è `1.0.<github.run_number>`; i binari già pubblicati non vengono sostituiti.
3. Impostare quella versione in `[versions]` di `gradle/libs.versions.toml` in UniApp.
4. Eseguire `python3 scripts/fetch_sdk_binaries.py`, poi le build dell'app.

Il catalogo è l'unica fonte delle versioni. Il downloader legge le coordinate
da `scripts/sdk_binaries.json`, trova le versioni nel catalogo e scarica la Release
`v<versione>` esatta. Controlla identità, versione, checksum, percorsi dell'archivio
e collisioni di classi Android. L'installazione sostituisce atomicamente il
repository locale; un errore conserva l'installazione precedente.

`.sdk-binaries/resolved.properties` registra la provenienza dei binari e alimenta
`AppInfoProvider`. Non modifica le versioni Gradle. Se il catalogo cambia e i binari
installati non corrispondono, Gradle richiede un nuovo download.

Servono Python 3.11+, JDK 21 e `GITHUB_TOKEN`/`GH_TOKEN` con accesso in lettura agli
SDK privati. I runner usano `SDK_READ_TOKEN`, con fallback a `DEPLOY_TOKEN`.

## Toolchain

| Componente | Versione |
| --- | --- |
| Gradle | 9.7.1, distribuzione verificata con SHA-256 |
| Kotlin / Compose compiler | 2.4.20 |
| AGP | 9.4.0 |
| JDK / bytecode JVM | 21 |
| Android compile / target | API 37 |
| Compose Multiplatform | 1.12.0 |
| Compose Android | 1.12.1 |
| Material 3 Multiplatform | 1.12.0-alpha03 |
| Material 3 Android | 1.5.0-alpha28 |
| Ktor | 3.5.2 |
| Coroutines / Serialization / Datetime | 1.11.0 / 1.11.0 / 0.8.0 |

Material 3 resta sul ramo che espone le API Expressive già usate. La nuova alpha
1.13 richiede anche il ramo Compose 1.13 alpha, quindi non viene introdotta insieme
all'aggiornamento della toolchain stabile. I cataloghi degli SDK restano autonomi.
I nomi dei moduli Kotlin Android sono espliciti: evitano `:` nei percorsi
`META-INF/*.kotlin_module` degli AAR con Kotlin 2.4.20.

Riferimenti verificati il 13 settembre 2026:
[Kotlin 2.4.20](https://kotlinlang.org/docs/whatsnew2420.html),
[AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes),
[compatibilità Compose](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html).
Le versioni delle librerie sono state confrontate con i metadati di Maven Central
e Google Maven; le versioni delle Actions con le Release dei rispettivi repository.

## Android e iOS

Il runner Android richiede le etichette `self-hosted`, `Linux`, `X64`. Installa JDK
21 e Android SDK, scarica le dipendenze dichiarate, esegue i test e produce gli APK
firmati. I workflow SDK di pubblicazione richiedono le stesse etichette e preparano
i propri binari indipendentemente da UniApp.

Il runner iOS usa `macos-26` con Xcode 26.6 selezionato prima di Kotlin/Native.
Compila l'app per simulatore e archivia l'app device. Lo schema `iosApp` è condiviso
nel repository. La fase Xcode `embedAndSignAppleFrameworkForXcode` compila il solo
framework dell'app usando gli SDK già scaricati. `Config.xcconfig` configura ricerca
e collegamento di `ComposeApp`; il team di firma non cambia il bundle identifier.
L'IPA del workflow è priva di firma e non è una distribuzione App Store.

Verifica locale dell'app:

```sh
python3 scripts/fetch_sdk_binaries.py
python3 scripts/validate_release.py
./gradlew :composeApp:testAndroidHostTest :androidApp:assembleDebug \
  :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
```

La compilazione Kotlin per iOS su Linux verifica i sorgenti e le KLIB. Il link
Apple, l'archivio Xcode e l'esecuzione nel simulatore richiedono macOS.

## Errori dei runner del 12 settembre

- Android: il test del libretto si aspettava ancora di escludere un'idoneità senza
  voto numerico, comportamento cambiato nel mapper. Le asserzioni verificano ora
  anche l'idoneità e il gruppo con anno sconosciuto.
- iOS: due ViewModel in `commonMain` usavano `toSortedMap`, API JVM. Ordinano ora
  le entry con `sortedBy`, disponibile anche su Kotlin/Native.
- Configurazione: i sorgenti SDK venivano inclusi automaticamente in locale e le
  versioni del catalogo sostituite con quelle dell'ultimo download. Entrambi i
  meccanismi sono stati rimossi.
