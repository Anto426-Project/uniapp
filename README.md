<p align="center">
  <img src="./iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/ios-marketing-1024@1x.png" alt="UniApp icon" width="120" height="120">
</p>

<h1 align="center">UniApp</h1>

<p align="center">
  <img alt="Kotlin Multiplatform" src="https://img.shields.io/badge/Kotlin%20Multiplatform-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
  <img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white">
  <img alt="iOS" src="https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=apple&logoColor=white">
</p>

<p align="center">
  <img src="./assets/divider.gif" width="440" height="40" alt="divider">
</p>

<p align="center">
  UniApp per gli studenti dell'Universita degli Studi del Molise.
  <br>
  Sviluppata da <strong>Anto426</strong> con Kotlin Multiplatform, Compose Multiplatform e Material 3.
</p>

<p align="center">
  <img alt="In sviluppo" src="https://img.shields.io/badge/In%20sviluppo-cb5a2e?style=for-the-badge&logo=kotlin&logoColor=white">
  <a href="./docs/feedback-gateway.md"><img alt="Feedback" src="https://img.shields.io/badge/Feedback-cb5a2e?style=for-the-badge&logo=github&logoColor=white"></a>
</p>

## <img src="./assets/icon.gif" width="42px" alt="about"> UniApp

UniApp nasce da un progetto indipendente con l'obiettivo di offrire agli studenti dell'Universita degli Studi del Molise un accesso piu semplice, ordinato e immediato ai principali servizi universitari. L'app riunisce in un'unica esperienza le funzioni piu utili per la vita universitaria:

- gestione della carriera e consultazione del libretto
- visualizzazione di esami, appelli e prenotazioni
- accesso alle informazioni dello studente e al QR badge
- consultazione di questionari e compilazione multi-pagina
- gestione delle prenotazioni e delle cancellazioni dei trasporti
- una base tecnica pensata per supportare l'evoluzione dell'app e le future distribuzioni

UniApp e sviluppata e mantenuta in modo indipendente e non e affiliata ufficialmente all'Universita degli Studi del Molise.

## <img src="./assets/icon2.gif" width="48px" alt="stack"> Stack

```sh
root@anto426: ~/UniApp (main⚡)$ stack --list

> language:
  - Kotlin 2.4.10

> ui:
  - Compose Multiplatform 1.12.0
  - Material 3

> platform:
  - Android (min SDK 29)
  - iOS (arm64, simulator arm64)

> tools:
  - Gradle
  - Android Studio
  - Xcode
```

## <img src="./assets/icon1.gif" width="48px" alt="modules"> Moduli e submodule

I moduli di UniApp sono collegati come Git submodule e vengono inclusi nella build principale tramite Gradle composite build.

| Modulo                   | Funzione                                                             | Repository                                                      |
| ------------------------ | -------------------------------------------------------------------- | --------------------------------------------------------------- |
| `liquid-monet`           | Componenti Compose, tema Monet e superfici optical glass             | [Anto426/Liquid-Monet](https://github.com/Anto426/Liquid-Monet) |
| `uni-sdk`                | Client KMP per API Cineca, backend UniApp, aggiornamenti e trasporti | Modulo interno                                                  |
| `secure-storage-sdk`     | Persistenza sicura multipiattaforma per credenziali e token          | Modulo interno                                                  |
| `firebase-connector-sdk` | Push notification e integrazione Firebase/FCM                        | Modulo interno                                                  |

I riferimenti ai submodule sono definiti in [.gitmodules](./.gitmodules). Per inizializzarli dopo un clone:

```sh
git submodule update --init --recursive
```

## <img src="./assets/icon1.gif" width="48px" alt="structure"> Struttura

```text
androidApp/                 Launcher Android e manifest
composeApp/                 Codice condiviso Compose Multiplatform
  src/commonMain/           UI e logica condivisa
  src/androidMain/          Integrazioni Android
  src/iosMain/              Integrazioni iOS
androidApp/src/main/        Entry point Android
libs/
  liquid-monet/              Submodule: tema e componenti UI
  uni-sdk/                   Submodule: API e logica servizi
  secure-storage-sdk/       Submodule: storage sicuro
  firebase-connector-sdk/   Submodule: notifiche Firebase
assets/                     Asset grafici del README
scripts/                    Utility di versioning e deploy
iosApp/                     Host application iOS
docs/                       Documentazione del progetto
```

<p align="center">
  <img src="./assets/divider.gif" width="440" height="40" alt="divider">
</p>

## Contributi e feedback

Per segnalazioni, idee o problemi, consulta la [guida al feedback](./docs/feedback-gateway.md). Prima di aprire una segnalazione verifica che il problema sia riproducibile sull'ultima build disponibile.
