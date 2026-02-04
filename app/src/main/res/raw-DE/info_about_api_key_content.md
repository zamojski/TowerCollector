[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Das Zugriffstoken (API-Key) ist eine spezieller Schlüssel, der Benutzern erlaubt, gesammelte Messungen an die OpenCellID-Datenbank hochzuladen.

Ein gültiger Key besteht aus den Ziffern 0-9 und den Buchstaben a-f. Je nach Registrierungsdatum kann das Format so aussehen:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 Zeichen, vorangestellt mit `pk.`, insgesamt 35 Zeichen
* `9743a66f914cc249efca164485a19c5c` - 32 Zeichen
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 Zeichen in 5 Gruppen, getrennt durch Bindestriche, insgesamt 36 Zeichen
* `9743a66f914cc2` - 14 Zeichen