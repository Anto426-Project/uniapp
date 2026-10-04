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
Una release desktop può contenere i pacchetti Linux, Windows e macOS insieme.

Gli SDK sono dipendenze già pubblicate nei propri repository: queste build li
scaricano con versioni esplicite e non li compilano o ripubblicano.

I workflow `Publish Android`, `Publish iOS` e `Publish Desktop` si possono avviare
anche manualmente indicando `source_run_id`. Se vuoto, selezionano l'ultima build
completata della propria piattaforma. Verificano origine e checksum prima della
pubblicazione; non accettano build di un'altra piattaforma.
