# Build e deploy per piattaforma

In GitHub Actions scegliere il workflow sul branch `main`:

| Build | Pacchetti | Publisher | Tag release |
| --- | --- | --- | --- |
| Build Android (APK) | Un APK release ARM64, firmato se la chiave è disponibile | Publish Android | `v<versione>+<versionCode>`; `android-v<versione>+<versionCode>-unsigned` senza firma |
| Build iOS (Kotlin Multiplatform) | IPA non firmata e bundle simulatore | Publish iOS | `ios-v<versione>+<versionCode>-unsigned` |

**Build All** avvia le due build indipendenti, mantenendo i contatori
Android/iOS esistenti. Il suo successo conferma soltanto gli avvii: i risultati
di compilazione si seguono nei workflow Build, quelli di distribuzione nei
workflow Publish.

## Passaggio dalla build alla pubblicazione

Il job finale di ogni build avvia esplicitamente il proprio publisher con
`workflow_dispatch` e l'esatto `source_run_id`. Il publisher attende la conclusione
positiva della build e verifica repository, branch, workflow, revisione e
checksum. Una build fallita o annullata non viene pubblicata.

Tutte le scritture su `uniapp-upstream`, incluso il sito, usano lo stesso gruppo
di concurrency, `uniapp-distribution`, con `queue: max`: i deploy attendono il
proprio turno senza cancellare altre piattaforme in coda.
Gli asset già pubblicati devono avere gli stessi checksum: non vengono sostituiti
con file diversi. Il manifest di aggiornamento Android continua ad accettare
soltanto APK firmati e impedisce downgrade.

Il workflow del sito (`Build and publish website`) è scollegato dalle pubblicazioni dell'app e si avvia manualmente con `workflow_dispatch` solo quando serve aggiornare il sito.
`release/builds.json` conserva la cronologia; `release/platforms.json` contiene
gli ultimi download Android e iOS. `docs/` contiene le copie servite dal sito.

## Recuperare build già compilate

I workflow Publish possono essere avviati manualmente con `source_run_id`.
Se vuoto, selezionano l'ultima build riuscita del proprio workflow Android o iOS.
Gli artefatti devono essere ancora disponibili e non scaduti.

Le dipendenze mobili usano versioni esplicite e binari SDK verificati.
Non vengono pubblicate release SDK né allegati `sdk-binaries*.json/.properties`
alle release UniApp.
