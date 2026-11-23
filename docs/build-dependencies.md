# Dipendenze e runner

UniApp consuma esclusivamente binari Maven. Non usa composite build, sostituzioni
con repository vicini o versioni `latest`. Per Android/iOS ciascuno dei quattro SDK compila e
pubblica autonomamente Android AAR, metadati KMP/Maven e KLIB iOS device/simulator.

## Aggiornare uno SDK

1. Modificare e verificare lo SDK nel suo repository.
2. Pubblicare una nuova versione tramite il workflow dello SDK. Il numero Maven
   è `1.0.<github.run_number>`; i binari già pubblicati non vengono sostituiti.
3. Impostare quella versione in `[versions]` di `gradle/libs.versions.toml` in UniApp.
4. Eseguire `python3 scripts/sdk/fetch_sdk_binaries.py`, poi le build dell'app.

Il catalogo è l'unica fonte delle versioni. Il downloader legge le coordinate
da `scripts/sdk/sdk_binaries.json`, trova le versioni nel catalogo e scarica la Release
`v<versione>` esatta. Controlla identità, versione, checksum, percorsi dell'archivio
e collisioni di classi Android. L'installazione sostituisce atomicamente il
repository locale; un errore conserva l'installazione precedente.

`.sdk-binaries/resolved.properties` registra la provenienza dei binari e alimenta
`AppInfoProvider`. Non modifica le versioni Gradle. Se il catalogo cambia e i binari
installati non corrispondono, Gradle richiede un nuovo download.

Servono Python 3.11+, JDK 21 e credenziali con accesso in lettura agli SDK privati.
Per i test locali si usa Temurin 21, come nei runner CI.
Per i download il resolver cerca, nell'ordine:

1. `GITHUB_TOKEN`, poi `GH_TOKEN` nelle variabili d'ambiente.
2. Il token del login locale di GitHub CLI, tramite
   `gh auth token --hostname github.com`.
3. Gli helper Git già configurati per `https://github.com`, tramite
   `git credential fill` senza richieste interattive.

Chi ha già configurato `gh auth login` o Git Credential Manager può quindi
eseguire il resolver senza copiare un token nel progetto. Il resolver conserva
le credenziali recuperate in memoria e non le scrive nei file o nei log.
L'autorizzazione HTTP viene inviata soltanto all'API HTTPS di GitHub e rimossa
quando un download viene reindirizzato a un altro host.

I runner usano `SDK_READ_TOKEN`, con fallback a `DEPLOY_TOKEN`.

## Toolchain

| Componente | Versione |
| --- | --- |
| Gradle | 9.9.1, distribuzione verificata con [SHA-256 ufficiale](https://gradle.org/release-checksums/) |
| Kotlin / Compose compiler | 2.4.21 |
| AGP | 9.5.1 |
| JDK / bytecode JVM | 21 |
| Android compile / target | API 37 |
| Compose Multiplatform | 1.12.2 |
| Compose Android | 1.12.1 |
| Material 3 Multiplatform | 1.12.0-alpha03 |
| Material 3 Android | 1.5.0-rc01 |
| Ktor | 3.6.1 |
| Coroutines / Serialization / Datetime | 1.11.0 / 1.13.0 / 0.8.0 |

Le versioni della tabella e delle dipendenze condivise dagli SDK sono allineate al
[catalogo UniApp](../gradle/libs.versions.toml), riferimento dell'aggiornamento del
18 novembre 2026. Le dipendenze specifiche di ciascun SDK restano definite nel
proprio catalogo.

Per la distribuzione Gradle 9.9.1, lo SHA-256
`d8a18a7a841dd5df825d35a685fad6f2d7f12ac0c280e57b67c27b4a520ba228`
è stato verificato nella [pagina ufficiale dei checksum](https://gradle.org/release-checksums/)
e nel [checksum della distribuzione binaria](https://services.gradle.org/distributions/gradle-9.9.1-bin.zip.sha256).
I nomi dei moduli Kotlin Android sono espliciti: evitano `:` nei percorsi
`META-INF/*.kotlin_module` degli AAR.

## Android e iOS

Il runner Android richiede le etichette `self-hosted`, `Linux`, `X64`. Installa JDK
21 e Android SDK, scarica le dipendenze dichiarate, esegue i test e produce un solo
APK release: firmato se la chiave è disponibile, altrimenti non firmato.
I workflow SDK richiedono le stesse etichette e preparano
i propri binari indipendentemente da UniApp.

Il runner iOS usa `macos-26` e verifica l'Xcode predefinito prima di Kotlin/Native.
Compila l'app per simulatore e archivia l'app device. Lo schema `iosApp` è condiviso
nel repository. La fase Xcode `embedAndSignAppleFrameworkForXcode` compila il solo
framework dell'app usando gli SDK già scaricati. `Config.xcconfig` configura ricerca
e collegamento di `ComposeApp`; il team di firma non cambia il bundle identifier.
Il workflow conserva come file sia l'IPA priva di firma sia il bundle simulatore.
Ogni piattaforma ha il proprio workflow di pubblicazione: `Publish Android` e
`Publish iOS`. Ogni esecuzione pubblica una sola versione della piattaforma
selezionata. `Build All` avvia le due build indipendenti.
Gli aggiornamenti Android continuano a usare gli APK firmati ufficialmente.

Verifica locale dell'app:

```sh
python3 scripts/sdk/fetch_sdk_binaries.py
python3 scripts/checks/validate_release.py
./gradlew :composeApp:testAndroidHostTest :androidApp:assembleDebug \
  :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
```

La compilazione Kotlin per iOS su Linux verifica i sorgenti e le KLIB. Il link
Apple, l'archivio Xcode e l'esecuzione nel simulatore richiedono macOS.
