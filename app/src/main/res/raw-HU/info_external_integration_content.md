[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
A Tower Collector olyan feladatokat ajánl ki, amelyeket más alkalmazások *szándékok* küldésével futtathatnak, ilyen például a Tasker, az Automate vagy a Llama. Az alábbiakban meghatározott összes tevékenységhez a *Broadcast* típus használata szükséges. Ellenőrizd az alkalmazás súgóját az integráció részleteivel kapcsolatban.
*Mint az alkalmazáson belül is, egyszerre csak egy feladatot futtathatsz!*

### Adatgyűjtés elindítása

Az adatgyűjtő a beállítások szerint lesz elindítva. Művelet:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Adatgyűjtés leállítása

Az adatgyűjtés azonnali le lesz állítva. Művelet:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Feltöltés elindítása

A feltöltés a konfigurációs beállítások használatával lesz elindítva. Művelet:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Feltöltés leállítása

A feltöltés megszakításra kerül. Művelet:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Exportálás elkezdése

Az exportálás a legutóbbi futás konfigurációs beállításaival lesz elindítva, a mérések meg lesznek tartva. Művelet:

`info.zamojski.soft.towercollector.EXPORT_START`

### Exportálás leállítása

Az exportálás megszakításra kerül. Művelet:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Az Android Oreo és újabb verziók esetén meg kell adnod az összetevő csomagnevét is:

`info.zamojski.soft.towercollector`