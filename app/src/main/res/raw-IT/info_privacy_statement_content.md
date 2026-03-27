<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
La privacy è molto importante, quindi l\'applicazione raccoglie e trasmette solo
le informazioni necessarie per fornire i servizi. L\'applicazione NON raccoglie,
memorizza o invia alcuna informazione che possa identificare personalmente
l\\\'utente, il suo dispositivo o qualsiasi altra informazione personale.

**Se non si accetta questa politica sulla privacy, si prega di non installare o
di disinstallare l\'applicazione. Rimuovere permanentemente l\'applicazione dal
dispositivo mobile equivale a terminare l\'uso dell\'applicazione.**

### Le funzioni principali

La funzionalità principale dell'applicazione è la raccolta degli identificativi
delle celle telefoniche, dell'intensità del segnale e della posizione
(coordinate) del dispositivo. Queste informazioni vengono memorizzate localmente
sul dispositivo e non saranno mai trasmesse senza il consenso esplicito
dell'utente. I dati raccolti possono essere esportati in un file memorizzato sul
dispositivo o condivisi con i progetti OpenCellID.org e BeaconDB. Per garantire
la protezione della privacy, l'applicazione NON utilizza alcun server proxy che
potrebbe accedere ai dati durante la trasmissione. Tutto il traffico verso
servizi esterni è crittografato tramite protocollo HTTPS.

**Per contribuire al progetto OpenCellID.org** è necessario un token di accesso
personale (chiave API) che deve essere inserito nelle preferenze
dell'applicazione. La chiave verrà utilizzata esclusivamente per caricare le
misurazioni nel database del progetto e non lascerà mai il dispositivo in nessun
altro caso. Chiunque conosca il token di accesso ha il pieno controllo sui dati
caricati, inclusa la possibilità di visualizzarli ed eliminarli. È tua
responsabilità proteggere la riservatezza del tuo token di accesso!

- [La privacy policy di
  OpenCellID.org](https://community.opencellid.org/privacy)
- [L'informativa sulla privacy di BeaconDB](https://beacondb.net/privacy/)

La maggior parte delle caratteristiche dell\'applicazione richiede il permesso
**di localizzazione in background** e comunque, per iniziare a raccogliere le
misure all\'avvio del dispositivo, il permesso **di localizzazione in
background** deve essere concesso. L\'autorizzazione alla posizione in
background è richiesta perché, in questo scenario, la raccolta viene avviata dal
sistema (in background), non da te (in primo piano).

### Le funzioni opzionali

La funzione **mappa** utilizza i servizi forniti da OpenStreetMap sotto [le
condizioni di utilizzo](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

La funzione di **controllo automatico degli aggiornamenti** si collega al server
per verificare se è disponibile una versione più recente dell'applicazione. La
richiesta contiene la versione dell'applicazione installata e viene eseguita
all'avvio dell'applicazione una volta al giorno.

La funzione **Contatta lo sviluppatore** creerà un nuovo messaggio di posta
elettronica nel client di posta elettronica selezionato. Il messaggio include le
seguenti informazioni tecniche sul dispositivo per aiutare nella risoluzione dei
problemi:

- La versione dell'applicazione
- La versione Android
- Marca e modello del device

La funzione **Preferenze di esportazione** o **Base dati di esportazione**
copierà le preferenze dell'applicazione o il database nella cartella dedicata
sul dispositivo. Se i file sono posizionati sulla memoria condivisa, a cui
qualsiasi altra applicazione ha accesso, allora i dati sensibili (come
l'OpenCellID Access Token) potrebbero non essere protetti.

La funzione **registra su file a livello di Debug** creerà file di log molto
dettagliati nella cartella dedicata sul vostro dispositivo. Il log può contenere
le informazioni sensibili, come la vostra posizione attuale e la torre cellulare
a cui il vostro dispositivo è collegato. Questa opzione dovrebbe essere usata
solo temporaneamente per la risoluzione dei problemi.

### Chi aiuta alla risoluzione dei problemi

L'applicazione raccoglie **le statistiche di utilizzo anonime**. I dati vengono
condivisi solo con lo sviluppatore, per aiutare a capire meglio come viene
utilizzata l'applicazione, quali sono i tempi di esecuzione delle varie funzioni
e quali sono le caratteristiche più popolari. I dati raccolti vengono utilizzati
per migliorare le prestazioni complessive, l'usabilità e l'esperienza
dell'utente. I dati sono raccolti utilizzando la libreria di terzi di Google
Analytics for Firebase fornita da Google sotto [la politica sulla privacy di
Google](https://policies.google.com/privacy).

L'applicazione ha incorporato il sistema **automatizzato di segnalazione di
crash**. I rapporti di guasto dell'applicazione sono condivisi solo con lo
sviluppatore e contengono informazioni sulla causa del crash e sulla
configurazione del dispositivo. NON include alcuna informazione che possa essere
utilizzata per identificare l'utente o il dispositivo.
