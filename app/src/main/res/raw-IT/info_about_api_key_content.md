<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Il token di accesso (chiave API) è una stringa speciale utilizzata per autorizzare l\'utente a caricare le misurazioni raccolte nel database OpenCellID.

La chiave corretta deve essere composta da cifre 0-9 e lettere a-f. A seconda della data di registrazione, il formato della chiave potrebbe essere simile a questo:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 caratteri, preceduti da `pk.`, per un totale di 35 caratteri
* `9743a66f914cc249efca164485a19c5c` - 32 caratteri
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 caratteri in cinque gruppi separati da un trattino, in totale 36 caratteri
* `9743a66f914cc2` - 14 caratteri