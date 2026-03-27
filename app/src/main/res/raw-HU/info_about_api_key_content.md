<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
A hozzáférési token (API-kulcs) egy speciális karakterlánc, amely felhatalmazza
a felhasználót a gyűjtött mérések OpenCellID adatbázisba való feltöltésére.

A helyes kulcsnak 0-9 számjegyekből és a-f betűkből kell állnia. A regisztráció
dátumától függően a kulcs formátuma így nézhet ki:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 karakter, előtte a `pk.`, összesen
  35 karakter
* `9743a66f914cc249efca164485a19c5c` - 32 karakter
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 karakter öt csoportban, kötőjellel
  elválasztva, összesen 36 karakter
* `9743a66f914cc2` - 14 karakter
