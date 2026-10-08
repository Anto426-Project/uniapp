## [2.0.15] - 2026-10-08 (build set by CI)

- UniSDK aggiornato alla release 1.0.25: 131 codici di errore stabili dichiarati in un unico file nell’SDK, con messaggi localizzati nell’app. La dipendenza mobile usa i binari pubblicati; il desktop compila la stessa revisione fissata come 1.0.25-desktop.1.
- Ripristinata la rotazione 3D delle card hero e del banner generico tramite Liquid Monet, rispettando Riduci movimento.
- Build e deploy separati per Android, iOS, Windows, Linux e macOS; Linux comprende Debian/Ubuntu e Arch. Avvio esplicito dei publisher, pubblicazione solo di build riuscite e indice dei download per piattaforma sul sito.
- In Tema, unica sezione per sfondo Liquid, fisica e animazioni tra pagine: selettore per gli otto preset animati dell'SDK, con Dissolvenza orizzontale predefinita; preferenze persistenti e verso coerente per navigazione e ritorno indietro. Il controllo globale Riduci movimento gestisce la disattivazione, senza un secondo interruttore per le pagine.
- Liquid Monet aggiornato alla versione 2.0.36; SDK desktop compilato dalla stessa revisione con versione 2.0.36-desktop.1.
- Unica transizione dell'SDK nel contenitore generale per pagine, sottopagine e ritorno indietro; cambi di sessione immediati e movimento ridotto gestito dall'SDK.
- Rimosse le animazioni locali e gli ingressi delle singole sezioni; l'indicatore di verifica aggiornamenti usa il componente dell'SDK. Aggiunto un controllo che impedisce nuovi preset o animazioni nelle schermate.
- Allineata la dipendenza Material3 Android alla pubblicazione multiplatform usata dall'SDK.
- Il download degli SDK verifica anche i documenti di licenza dichiarati nel manifest dei nuovi archivi, mantenendo i controlli sui checksum e sui file inattesi.

## [2.0.15-desktop.1] - 2026-10-07

- Launcher desktop aggiornato alla versione 2.0.15, con gli stessi preset Liquid Monet e la stessa transizione condivisa dell'app mobile.
- Pacchetti desktop Linux, Windows e macOS prodotti dalla build indipendente della piattaforma.

## [2.0.14-desktop.1] - 2026-10-04

- Launcher desktop JVM per Linux, Windows e macOS, con storage cifrato nel portachiavi nativo e importazione QR/barcode da immagine.
- SDK desktop compilati localmente da revisioni fissate, con versioni esplicite e checksum verificati; rimosse le release SDK desktop e gli allegati sdk-binaries dalle release dell'app.
- Workflow desktop con test e pacchetti Debian/Ubuntu, Arch Linux (.pkg.tar.zst), Windows e macOS, selezionabili singolarmente.
- Build e pubblicazione indipendenti per Android, iOS e PC; Build All avvia i workflow specifici. Una sola versione per piattaforma: Android release firmata quando la chiave è disponibile, altrimenti unsigned. Gli aggiornamenti Android continuano a usare le release firmate ufficialmente.

## [2.0.14] - 2026-09-29 (build set by CI)

- **Avvio e Sblocco**: il navigatore mostra il caricamento finché la sessione non è risolta e apre solo la destinazione consentita; la schermata di sblocco appare solo per gli account protetti, con password UniApp e pulsante per richiamare l'autenticazione del dispositivo senza prompt automatico. “Usa un altro account” apre il login.
- **Login**: il passaggio alle schermate dell’account non lascia visibile per un istante il fondo scuro dell’app.
- **Caricamenti**: una richiesta dati interrotta termina con un errore recuperabile e viene ritentata, senza lasciare le sezioni in caricamento continuo.
- **Account**: il cambio sessione mantiene separati identità, credenziali, carriere e dati salvati anche quando il login autentica un account diverso da quello in attesa di riaccesso; l'elenco carriere temporaneo appartiene al solo login che lo ha generato. La carriera restituita dal portale viene verificata prima di attivarla e i profili con identificativi incompleti non vengono più uniti per somiglianza.
- **Sessioni e dati**: un solo gestore della sessione attiva e un archivio dati per account e carriera; rimossi i componenti che inoltravano soltanto le chiamate. Durante il cambio il navigatore mostra il caricamento e interrompe le richieste del profilo precedente. Il gestore del cambio account resta attivo quando Login o Impostazioni vengono chiuse, così l'attivazione può completarsi.
- **Nuovo accesso richiesto**: account, credenziali, ticket, preferenze e cache locali vengono cancellati una sola volta al primo avvio di questa build. Nessun vecchio ticket o profilo viene convertito.
- **UniSDK 1.0.23**: identità della carriera docente basata sugli ID del portale e completamento della selezione quando il portale richiede una seconda risposta di autenticazione.
- **Identità**: rimossa la sostituzione del nome e della foto degli account reali con quelli dell'autore del progetto.
- **Prenotazioni Trasporti**: il calendario mostra il mese intero, ma permette di scegliere solo i giorni feriali da domani fino a 15 giorni. Le corse già prenotate restano visibili e non selezionabili; dopo una prenotazione la disponibilità si aggiorna subito. Rinnovate le card di selezione della direzione.
- **Biglietti**: il pulsante per annullare la corsa rifrange il contenuto sottostante; aggiornata la resa del codice a barre.
- **Area Docente**: card e sezioni con altezze più coerenti, metriche bilanciate e icone dedicate.
- **Badge Accademico**: nuova grafica del codice a barre con cornice Monet e stato attivo.
- **Home e Temi**: card notizie più compatte e selettore del motore grafico aggiornato.
- **Carriere**: cambio profilo disponibile dalla barra superiore delle sezioni principali, con gestione dell'elenco delle carriere restituito dal portale. Il cambio mostra l'attesa e segnala il motivo del rifiuto invece di chiudersi senza feedback.
- **Informazioni**: corretto l'allineamento della versione e del nome dell'app nel banner.
- Icone dell'app aggiornate e variante di sviluppo separata dalla versione installata.

## [2.0.13] - 2026-09-27 (build set by CI)

- Notizie più fluide: feed preparato una sola volta, card Home di altezza stabile e dettaglio formattato con link alla fonte.
- Cache e navigazione separate per account e profilo; consenso notifiche associato all’account attivo.
- Banner Info app e Aggiornamenti più leggibili; verifica del pacchetto collegata allo stato reale dell’updater.
- Storage locale reimpostato una sola volta per questa migrazione, con avviso da confermare all’apertura. Sarà necessario accedere di nuovo.
- Liquid Monet 2.0.32 e UniSDK 1.0.18; log trasporti senza contenuti delle risposte del portale.

## [2.0.12] - 2026-09-25 (build set by CI)

- Rubrica con numeri e dettagli completi, notizie filtrate per fonte e card Home uniformate.
- Badge accademico con un'unica tessera e barcode integrato; valore del biglietto più leggibile sotto le barre.
- Prenotazioni navetta consentite solo nei feriali da domani a 15 giorni; selezioni multiple e andata/ritorno inviate come corse separate, con esito parziale visibile.
- Tratte normalizzate nel UniSDK: andata prima del ritorno e percorso del ritorno mostrato in direzione inversa in elenco, dettaglio e riepilogo.
- Il toggle biometrico torna allo stato precedente se la conferma viene annullata; l'attivazione viene mostrata solo dopo il salvataggio riuscito.
- Migliorata la gestione del task Android quando l'app viene riaperta dal launcher o da una notifica.

## [2.0.11] - 2026-09-25 (build set by CI)

- Il codice a barre del biglietto mostra il valore sotto le barre; rimossa la vista digitale duplicata sotto la tessera.

## [2.0.10] - 2026-09-25 (build set by CI)

- Biglietti salvati e disponibili offline anche se il portale risponde con errori o elenchi incompleti.
- Dati del viaggio corretti e prenotazioni ordinate per data e ora.
- Rimosso il collegamento ridondante al biglietto ufficiale dal dettaglio.

## [2.0.9] - 2026-09-23 (build set by CI)

- Riprogettata la schermata di prenotazione corse con selezione direzione in card tematiche e pass di riepilogo con posto garantito.
- Rinnovata la vista del biglietto originale e dei codici QR con contenitori LiquidSheet compatti ad altezza ridotta.
- Aggiunte azioni rapide (email e chiamata) nella scheda contatto e migliorata la navigazione alla rubrica dalla schermata corso.
- Armonizzata la spaziatura delle card notizie con il design system Liquid Monet.

## [2.0.8] - 2026-09-15 (build set by CI)

- Corretti gli angoli della cornice del QR del badge, mantenendo dimensioni e leggibilità del codice.
- Rubrica completa con tutti i numeri di telefono, indirizzo ed edificio separati e azioni email e chiamata.
- Corretti ricerca, selezione degli omonimi e contatti senza sede; aggiornato UniSDK per preservare tutti i dati della rubrica.

## [2.0.7] - 2026-09-13 (build set by CI)

- Badge accademico rinnovato con trama grafica, cornice colorata e QR artistico arrotondato.
- Rimosso il contenuto testuale del QR dalla card del badge.
- Recupero del QR accademico anche per le sessioni già salvate, con UniSDK 1.0.11.

## [2.0.6] - 2026-09-13 (build set by CI)

### Trasporti e Biglietti
- Aggiunto il salvataggio offline, il caching e l'ispezione ad alta definizione per i biglietti del trasporto universitario.
- Supporto alla decodifica automatica dei codici a barre dai biglietti originali con passaggio fluido tra vista digitale e immagine originale.

### Badge e Codici
- Introdotto motore cross-platform per la generazione e decodifica di codici QR e a barre (Code 128, QR) conforme agli standard.
- Integrata la generazione dinamica e fedele del QR code per il badge accademico dello studente.

### SDK ed Ecosistema
- Aggiornato SDK Liquid Monet alla versione 1.0.13 e SDK UniSDK alla 1.0.10.

## [2.0.5] - 2026-09-13 (build set by CI)

### UI e Didattica
- Riprogettata la schermata di dettaglio del corso con collegamento ai dati accademici reali: Settore Scientifico Disciplinare (SSD), tipologia attività, lingua, crediti CFU, prerequisiti, obiettivi formativi e programma esteso.
- Aggiunta la scheda docente con contatti ufficiali, recapiti, orari di ricevimento, sede di dipartimento e navigazione rapida al profilo del professore.
- Visualizzazione completa degli esami nel libretto universitario, compresi tirocini, idoneità linguistiche e giudizi (con indicatori dedicati "Idoneo", "Superato", "Approvato").
- Allineato il grafico di simulazione e distribuzione dei crediti CFU nella schermata Voti con lo stile e l'istogramma delle statistiche di carriera.

### Servizi e Presenze
- Riorganizzata la schermata Servizi con griglie bilanciate e speculari (4 servizi principali in griglia 2x2 e 6 portali universitari in griglia 3x2).
- Rimosso il titolo ridondante "Servizi principali" e aggiunti distanziatori ottici per evitare sovrapposizioni con la barra di navigazione fluttuante.
- Rinnovata l'esperienza di registrazione delle presenze: sostituito il dialog con il componente standard `LiquidDialog` dell'SDK e integrato lo scanner QR a schermo intero.
- Riorganizzati i pulsanti di convalida e chiusura in orientamento verticale per evitare troncamenti di testo.
- Rimosso il badge "Live aula" superfluo dalle attività.

### Design e Temi Liquid Monet
- Eliminati i colori scuri e le sfumature cupe dal Color Lab e dalla personalizzazione dei temi in favore di tonalità luminose ed espressive Monet.
- Aggiunto il supporto per l'inserimento manuale e l'incolla da appunti di codici colore esadecimali (HEX).
- Rimosso l'elenco delle note di rilascio (changelog) dal foglio di notifica aggiornamenti dell'app per una schermata di avviso più pulita e diretta.

### Infrastruttura e Compilazione
- Aggiornato Android Gradle Plugin ad AGP 9.4.0 e allineato Gradle Wrapper a 9.7.1 su tutti i moduli dell'ecosistema.
- Introdotto il supporto automatico alle build composite (`includeBuild`) per la risoluzione e il debug immediato degli SDK locali.


## [2.0.4] - 2026-09-11 (build set by CI)

### UI e funzionalità App
- Semplificata la conferma password nelle impostazioni dell'app con il pulsante "Conferma".
- Riorganizzato il banner informativo dell'app con versione, build e accesso rapido alle note di rilascio.
- Introdotto il nuovo selettore rapido degli account con card profilo dedicata e gestione avatar.
- Riprogettati i banner di aggiornamento con hero header coerente e feedback di avanzamento.
- Armonizzato lo stile grafico delle card notizie nella schermata principale con il design Liquid.

### CI / Build infrastruttura
- Il `versionCode` Android usa `uniapp.versionCodeBase + GITHUB_RUN_NUMBER` (base attuale: 1000), eliminando il bump manuale e mantenendo la progressione rispetto alle vecchie build.
- I 4 SDK pubblicano AAR Android ottimizzati con R8, metadati KMP e KLIB iOS su **GitHub Packages** e come archivio Maven nelle **GitHub Releases**, ad ogni push sul rispettivo branch principale, con versione `1.0.<run_number>`.
- UniApp scarica le Release SDK con autenticazione anche per gli allegati privati, verifica gli hash e risolve le versioni esatte da un repository Maven locale. Versioni e revisioni vengono conservate con l'artefatto della build. Rimossi i submodule e i composite build degli SDK.
- Le classi offuscate di ogni SDK usano uno spazio di nomi distinto per evitare collisioni nell'APK; il resolver verifica anche l'assenza di classi duplicate prima di aggiornare la cache.
- Eliminato il workflow ridondante `build-shared-libs.yml`; le build Android e iOS restano manuali e riusano i binari SDK già compilati.
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
- Ripristinati i test Android prima della pubblicazione.
