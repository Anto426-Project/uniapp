# UniApp per PC

UniApp dispone di un launcher Compose Desktop (`desktopApp`) e di un target JVM
(`composeApp:desktop`) per Linux, incluso Arch Linux, Windows e macOS. La UI e i servizi universitari
sono condivisi con Android/iOS. La finestra è ridimensionabile; Esc torna indietro.

## Compilazione

Usare JDK 21. Prima scaricare le release mobili già dichiarate nel catalogo:

```sh
python3 scripts/fetch_sdk_binaries.py
```

Gli SDK desktop vengono compilati localmente dai commit fissati in
`scripts/desktop_sdk_sources.json`, con le versioni esplicite del catalogo.
Per preparare tutte le dipendenze e avviare l'app:

```sh
python3 scripts/fetch_sdk_binaries.py --desktop
./gradlew :desktopApp:run
```

I sorgenti fissati vengono scaricati in `.sdk-sources`; i binari compilati restano
in `.sdk-binaries` e non vengono pubblicati come release SDK o allegati all'app.
Per sviluppare gli SDK nei quattro checkout adiacenti è disponibile anche
`python3 scripts/build_desktop_sdks.py`. Non vengono usati composite builds o
risoluzioni `latest`. Il manifest registra versioni, revisioni e checksum.

La compilazione automatica è in `.github/workflows/build-desktop.yml`. L'input
`desktop_os` sceglie `linux` (Debian/Ubuntu), `archlinux`, `windows`, `macos` oppure
`all` per tutti e quattro i pacchetti. `Publish Desktop`
pubblica un'unica versione PC con i pacchetti prodotti in
`Anto426-Project/uniapp-upstream`. Android e iOS hanno workflow indipendenti;
`Build All` li avvia insieme senza compilare o pubblicare un'altra copia.
La [guida ai workflow](build-workflows.md) descrive gli avvii specifici.

## Pacchetti

```sh
./gradlew :desktopApp:createDistributable
./gradlew :desktopApp:packageDistributionForCurrentOS
```

I pacchetti includono il runtime Java. Creare `.deb`/`.rpm` su Linux, `.msi` su
Windows e `.dmg` su macOS; il packaging non è cross-platform.

Su Arch Linux, dopo `createDistributable`:

```sh
python3 scripts/prepare_archlinux.py
cd desktopApp/build/compose/binaries/main/archlinux
makepkg --nodeps --noconfirm
```

Il workflow usa `makepkg` nell'immagine ufficiale Arch Linux `base-devel`.
Il pacchetto x86_64 `.pkg.tar.zst` include Java, il comando `uniapp` e una voce nel
menu applicazioni. Scaricare il pacchetto dalla release PC e installarlo con:

```sh
sudo pacman -U ./UniApp-2.0.15-desktop.1-archlinux.pkg.tar.zst
uniapp
```

Le dipendenze di sistema vengono risolte da `pacman`; per il vault serve anche un
servizio Secret Service/KWallet attivo nella sessione. Il pacchetto è distribuito
nelle release UniApp; non è registrato su AUR.

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
