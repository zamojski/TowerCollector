<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector espone le attività che devono essere eseguite da altre applicazioni che inviano *segnalazioni (intents)* come Tasker, Automate o Llama. Devi utilizzare il tipo *Trasmissione (Broadcast)* per tutte le attività definite di seguito. Per ulteriori informazioni su come eseguire tale integrazione, controllare la guida delle applicazioni.
*Come all\'interno dell\'app, non puoi avviare più in una attività per volta!*

### Avvia Raccolta Dati

Il servizio di raccolta dati sarà avviato utilizzando le impostazioni definite in Preferenze. Azione:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Arresta Raccolta Dati

Il servizio di raccolta dati sarà fermato immediatamente. Azione:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Avvia Caricamento

Il caricamento sarà avviato utilizzando quanto impostato in Preferenze. Azione:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Interrompi caricamento

Il caricamento sarà interrotto immediatamente. Azione:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Inizia l'esportazione

L'esportazione verrà avviata utilizzando la configurazione dell'ultima esecuzione, le misurazioni saranno mantenute. Azione:

`info.zamojski.soft.towercollector.EXPORT_START`

### Ferma l'esportazione

L'esportazione verrà annullata subito. Azione:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Su Android Oreo e versioni successive è necessario definire ulteriormente il nome del pacchetto del componente:

`info.zamojski.soft.towercollector`