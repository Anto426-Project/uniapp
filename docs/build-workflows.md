# Build per piattaforma

In GitHub Actions scegliere il workflow della piattaforma e premere **Run workflow** sul branch `main`:

| Workflow | File prodotti | Pubblicazione automatica |
| --- | --- | --- |
| Build Android (APK) | Un APK release, firmato se la chiave è disponibile oppure non firmato | Publish Android |
| Build iOS (Kotlin Multiplatform) | IPA non firmata e bundle simulatore della stessa versione | Publish iOS |
| Build Desktop | Installer e archivio portabile per i sistemi scelti con `desktop_os` | Publish Desktop |

**Build All** avvia questi tre workflow indipendenti. Il suo risultato indica
l'avvio; compilazione e pubblicazione si seguono nelle esecuzioni delle singole
piattaforme. Mantiene i contatori Android/iOS delle build specifiche.

Ogni piattaforma pubblica una sola versione per esecuzione. Android non produce
una seconda release debug o unsigned quando è disponibile quella firmata.
Una release desktop può contenere insieme i pacchetti Debian/Ubuntu, Arch Linux,
Windows e macOS. Per compilare solo Arch scegliere `desktop_os: archlinux` in
**Build Desktop**. Il file `.pkg.tar.zst` si installa con `pacman -U`.

Le dipendenze mobili vengono scaricate dalle versioni esplicite già dichiarate.
Gli SDK desktop vengono compilati da commit fissati in
`scripts/desktop_sdk_sources.json`, con gli stessi numeri Maven del catalogo,
senza pubblicare release SDK. I file `sdk-binaries*.json/.properties` non vengono
allegati alle release UniApp. I checksum dei pacchetti e il contesto della build
restano disponibili.

I workflow `Publish Android`, `Publish iOS` e `Publish Desktop` si possono avviare
anche manualmente indicando `source_run_id`. Se vuoto, selezionano l'ultima build
completata della propria piattaforma. Verificano origine e checksum prima della
pubblicazione; non accettano build di un'altra piattaforma.
