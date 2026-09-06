# Segnalazioni su GitHub

Le segnalazioni si aprono da Informazioni app → Segnala un bug su GitHub.
L’app prepara una bozza di issue per `Anto426-Project/uniapp` e apre il browser.
L’utente accede a GitHub, rivede il testo e decide se pubblicarlo. Aprire il browser non significa aver inviato la segnalazione.

La bozza contiene versione/build dell’app, piattaforma, sistema operativo e versioni/revisioni dei moduli inclusi. Non allega log, token, credenziali, nomi, identificativi universitari o dati della carriera. L’utente deve controllare anche screenshot e testo che aggiunge: la issue è pubblica.

Non è richiesto alcun token GitHub nell’app né un gateway proprietario per il feedback. Il precedente metodo SDK `submitBugReport` è rimosso: i chiamanti devono aprire `ProjectInfo.bugReportUrl()` tramite il gestore URL della piattaforma.
