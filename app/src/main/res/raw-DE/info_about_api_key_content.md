<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Der Zugriffstoken (API-Schlüssel) ist eine spezielle Zeichenkette, die den
Benutzer dazu berechtigt, die gesammelten Messwerte in die OpenCellID-Datenbank
hochzuladen.

Der korrekte Schlüssel muss aus den Ziffern 0–9 und den Buchstaben a–f bestehen.
Je nach Registrierungsdatum kann das Schlüsselformat folgendermaßen aussehen:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 Zeichen, beginnend mit `pk.`,
  insgesamt 35 Zeichen
* `9743a66f914cc249efca164485a19c5c` - 32 Zeichen
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 Zeichen in fünf durch Bindestriche
  getrennten Gruppen, insgesamt 36 Zeichen
* `9743a66f914cc2` - 14 Zeichen
