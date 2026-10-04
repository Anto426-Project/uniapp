# UniApp per PC

UniApp dispone di un launcher Compose Desktop (`desktopApp`) e di un target JVM
(`composeApp:desktop`) per Linux, Windows e macOS. La UI e i servizi universitari
sono condivisi con Android/iOS. La finestra è ridimensionabile; Esc torna indietro.

## Compilazione

Usare JDK 21. Prima scaricare le release mobili già dichiarate nel catalogo:

```sh
python3 scripts/fetch_sdk_binaries.py
```

Gli SDK desktop sono pubblicati separatamente, con versioni esplicite nel catalogo.
Per scaricare anche i loro binari verificati e avviare l'app:

```sh
python3 scripts/fetch_sdk_binaries.py --desktop
./gradlew :desktopApp:run
```

Per sviluppare gli SDK nei quattro checkout adiacenti è disponibile anche
`python3 scripts/build_desktop_sdks.py`. Non vengono usati composite builds o
risoluzioni `latest`. Il manifest registra versioni, revisioni e checksum.

La compilazione automatica è in `.github/workflows/build-desktop.yml`, con
pacchetti Linux, Windows e macOS. `publish-build.yml` pubblica tutti gli artefatti
prodotti, anche non firmati, in `Anto426-Project/uniapp-upstream`. Include anche
Android release firmata (quando la chiave è disponibile), release sempre non
firmata, debug e iOS dispositivo/simulatore. Le varianti non firmate
sono release di anteprima; gli APK debug usano una chiave di sviluppo e le IPA
richiedono firma/provisioning prima dell'installazione su un dispositivo.

## Pacchetti

```sh
./gradlew :desktopApp:createDistributable
./gradlew :desktopApp:packageDistributionForCurrentOS
```

I pacchetti includono il runtime Java. Creare `.deb`/`.rpm` su Linux, `.msi` su
Windows e `.dmg` su macOS; il packaging non è cross-platform.

## Funzioni di sistema

- Account e dati vengono cifrati con AES-GCM, con una chiave distinta per vault
  conservata nel portachiavi nativo (Secret Service/KWallet su Linux, Credential
  Manager su Windows, Keychain su macOS). Nessun ripiego su chiavi in chiaro.
  Su Linux serve un servizio portachiavi disponibile nella sessione utente.
- I dati risiedono in `$XDG_DATA_HOME/uniapp`, `%LOCALAPPDATA%/UniApp` oppure
  `~/Library/Application Support/UniApp`. Per prove isolate si può passare
  `-Duniapp.dataDir=/percorso/temporaneo` alla JVM.
- Lo scanner PC legge QR e codici a barre da immagini PNG/JPEG; resta disponibile
  l'inserimento manuale. La webcam non è integrata.
- La password dell'app è disponibile; la biometria desktop non è integrata.
- FCM/APNs non registrano il desktop: le notifiche push risultano non supportate.
- Gli APK non vengono proposti come aggiornamenti desktop. Le distribuzioni PC
  si aggiornano tramite i pacchetti della pagina Releases; non esiste ancora un
  updater automatico desktop.

L'avvio Linux e i test JVM non verificano accesso con credenziali reali, servizi
universitari, né esecuzione o integrazioni native su Windows/macOS.
