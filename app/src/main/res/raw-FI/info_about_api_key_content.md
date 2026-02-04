[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Access token (API-avain) on erityinen merkkijono, jota käytetään valtuuttamaan käyttäjä lähettämään kerättyjä mittauksia OpenCellID-tietokantaan.

Oikean avaimen tulee koostua numeroista 0-9 ja kirjaimista a-f. Rekisteröintipäivästä riippuen avaimen muoto voi näyttää tältä:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 merkkiä, jota edeltää `pk.`, yhteensä 35 merkkiä
* `9743a66f914cc249efca164485a19c5c` - 32 merkkiä
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 merkkiä viidessä ryhmässä, jotka on erotettu viivalla, yhteensä 36 merkkiä
* `9743a66f914cc2` - 14 merkkiä