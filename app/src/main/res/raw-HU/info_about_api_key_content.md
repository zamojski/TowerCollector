[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
A hozzáférési token (API kulcs) egy speciális karakterlánc, amely feljogosítja a felhasználót a begyűjtött mérések feltöltésére az OpenCellID adatbázisba.

A helyes kulcs 0-9 közti számjegyeket, és a-f közti betűket tartalmaz. A regisztráció dátumától függően a kulcs a következőféleképpen nézhet ki:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 karakter, előtte `pk.`, összesen 35 karakter
* `9743a66f914cc249efca164485a19c5c` - 32 karakter
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 karakter kötőjellel elválasztva, öt csoportban, összesen 36 karakter
* `9743a66f914cc2` - 14 karakter