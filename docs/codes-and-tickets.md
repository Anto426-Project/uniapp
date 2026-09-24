# Badge, QR e biglietti

`CodeGenerator` genera painter vettoriali condivisi da Android e iOS:

```kotlin
val generator = CodeGenerator()
val badge = generator.qrCode(payload, QrCodeStyle.Artistic)
val barcode = generator.barcode(payload, BarcodeFormat.Code128)
```

Il QR artistico usa moduli raccordati, occhi con angoli asimmetrici e inchiostri
blu e verde scuro su bianco. Mantiene quattro moduli di margine e correzione M.
La card aggiunge una trama di curve e una cornice colorata; non mostra il valore
codificato sotto il QR. Il painter va conservato con `remember(payload, style)`.

## Contenuto del badge

Il badge ufficiale è stato acquisito dal telefono e decodificato. Il contenuto è
`matricola|cognome|nome|corso`, con maiuscole, spazi e zeri iniziali conservati.
Il SDK usa il valore esplicito delle API, se presente; altrimenti lo compone dai
campi accademici separati del profilo. Le sessioni precedenti recuperano questi
campi dal profilo autenticato, verificando la carriera. Un nome personalizzato
nell'interfaccia non modifica il contenuto del badge.

La cache `student-details-v2` aggiorna i dati delle installazioni precedenti.
Screenshot e contenuti personali di riferimento rimangono in `.tmp`, esclusa da Git.

## Biglietti dei trasporti

`TransportService.loadTransportTicketImage` scarica l'immagine usando la sessione
autenticata dei trasporti. UniApp salva i byte originali nello storage cifrato
dell'account, con una chiave distinta per carriera e prenotazione. L'immagine
salvata viene riaperta anche senza rete; «Scarica di nuovo» richiede una nuova copia.
Gli originali vengono archiviati quando la prenotazione appare nell'elenco, senza
attendere l'apertura del dettaglio. Un errore di download non elimina una copia già
salvata; il download viene riprovato al prossimo aggiornamento.

L'elenco delle prenotazioni è conservato nello stesso vault cifrato. Errori HTTP,
risposte incomplete e paginazione interrotta non sostituiscono l'ultima copia
valida. Una prenotazione e la sua immagine vengono rimosse dopo una cancellazione
confermata, oppure quando un elenco completo e valido del portale non la include
più. La sola età del biglietto non lo elimina; un vecchio biglietto ancora presente
nel portale resta disponibile.

`createCodeReader().read(imageBytes)` legge i codici sul dispositivo: ZXing su
Android e Vision su iOS. Conserva contenuto e simbologia. QR, Code 128/39/93,
EAN-13/8, UPC-A/E, ITF e Codabar possono essere rigenerati; dati binari, GS1 e
formati non riproducibili richiedono l'originale. Su iOS la rigenerazione da Vision
è limitata ai contenuti ASCII stampabili per preservare fedelmente il payload.

La schermata offre due pulsanti distinti: «Mostra originale», con zoom, e
«Mostra codice digitale». Il primo resta utilizzabile se la lettura del codice
fallisce. La vista digitale include i codici letti, senza inventarne uno dal
numero di prenotazione. Le schede del catalogo delle linee non generano biglietti.

Il download supporta PNG/JPEG/GIF/WebP diretti e HTML con una sola immagine;
pagine ambigue, autenticazione scaduta o formati diversi producono un errore e
la copia locale rimane disponibile. Limite: 3 MiB per originale.

Il dettaglio usa la data, l'ora, il numero e la tratta della prenotazione restituita
dal portale. Un'etichetta come `lun 28/09/2026` viene mostrata come `28/09/2026`;
`lun` da solo non è una data. La lista è ordinata per data e ora crescenti.

## Verifica

I test decodificano i pixel generati con un lettore indipendente, comprese le
varianti artistiche a diverse dimensioni. Verificano inoltre rotazione, GS1,
immagini illeggibili e mantenimento dell'originale dopo errori o aggiornamenti offline.

Per verificare un'immagine personale, senza aggiungerla al repository:

```sh
UNIAPP_CODE_REFERENCE_IMAGE=/percorso/privato/badge.png \
  ./gradlew :composeApp:testAndroidHostTest --tests 'com.anto426.uniapp.codes.*'
```

`UNIAPP_CODE_REFERENCE_PREVIEW` può indicare un file PNG privato in cui esportare
il codice rigenerato. Il confronto include anche lo stile artistico. Queste
verifiche non attestano l'accettazione di un biglietto da parte del servizio trasporti.
