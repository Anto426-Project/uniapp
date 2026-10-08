# Build e deploy per piattaforma

In GitHub Actions scegliere il workflow sul branch `main`:

| Build | Pacchetti | Publisher | Tag release |
| --- | --- | --- | --- |
| Build Android (APK) | Un APK release ARM64, firmato se la chiave è disponibile | Publish Android | `v<versione>+<versionCode>`; `android-v<versione>+<versionCode>-unsigned` senza firma |
| Build iOS (Kotlin Multiplatform) | IPA non firmata e bundle simulatore | Publish iOS | `ios-v<versione>+<versionCode>-unsigned` |
| Build Windows | Installer MSI e ZIP portabile | Publish Windows | `windows-v<versione>+<runNumber>` |
| Build Linux | Pacchetti Debian/Ubuntu e Arch, archivi portabili | Publish Linux | `linux-v<versione>+<runNumber>` |
| Build macOS | Installer DMG e archivio portabile | Publish macOS | `macos-v<versione>+<runNumber>` |

Windows e Linux hanno contatori, esecuzioni, release e download distinti. macOS
conserva un flusso autonomo. Il workflow riutilizzabile `build-desktop.yml` contiene
solo la compilazione condivisa; non è un punto di avvio manuale e non produce una
release desktop aggregata.

**Build Linux** accetta `linux_package`: `all` (predefinito), `linux` per
Debian/Ubuntu oppure `archlinux`. Se si richiedono entrambi, entrambi devono
riuscire prima di avviare la pubblicazione. Il pacchetto Arch si installa con
`pacman -U`.

**Build All** avvia le cinque build indipendenti, mantenendo i contatori
Android/iOS esistenti. Il suo successo conferma soltanto gli avvii: i risultati
di compilazione si seguono nei workflow Build, quelli di distribuzione nei
workflow Publish.

## Passaggio dalla build alla pubblicazione

Il job finale di ogni build avvia esplicitamente il proprio publisher con
`workflow_dispatch` e l'esatto `source_run_id`. Il publisher attende la conclusione
positiva della build e verifica repository, branch, workflow, revisione, varianti
e checksum. Una build fallita o annullata non viene pubblicata. Non si dipende
più da una catena di eventi `workflow_run`.

Tutte le scritture su `uniapp-upstream`, incluso il sito, usano lo stesso gruppo
di concurrency, `uniapp-distribution`, con `queue: max`: i deploy attendono il
proprio turno senza cancellare altre piattaforme in coda.
Gli asset già pubblicati devono avere gli stessi checksum: non vengono sostituiti
con file diversi. Il manifest di aggiornamento Android continua ad accettare
soltanto APK firmati e impedisce downgrade.

Dopo ogni pubblicazione riuscita viene avviato il workflow del sito. I download
Windows, Linux, iOS e macOS leggono `platforms.json`; ogni variante Linux conserva
la propria versione quando si aggiorna soltanto Debian o soltanto Arch.
`release/builds.json` conserva la cronologia; `release/platforms.json` contiene
l'indice corrente. `docs/` contiene le copie servite dal sito.

## Recuperare build già compilate

I workflow Publish possono essere avviati manualmente con `source_run_id`.
Se vuoto, selezionano l'ultima build riuscita del proprio workflow. Un ID esplicito
del vecchio **Build Desktop** è accettato dai publisher Windows, Linux e macOS:
ciascuno scarica solo i propri pacchetti e crea il proprio tag senza ricompilare.
Le release desktop precedenti restano disponibili; nessun asset viene cancellato.
Gli artefatti devono essere ancora disponibili e non scaduti.

Le dipendenze mobili usano versioni esplicite; gli SDK desktop vengono compilati
dai commit fissati in `scripts/desktop_sdk_sources.json`. Non vengono pubblicate
release SDK né allegati `sdk-binaries*.json/.properties` alle release UniApp.
