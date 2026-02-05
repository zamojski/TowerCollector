<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector bietet Tasks an, die von anderen Apps wie Tasker, Automate oder Llama mit Hilfe von *Intents* aufgerufen werden können. Dabei muss für alle unten beschriebenen Aktivitäten der Typ *Broadcast* verwendet werden. Weitere Informationen dazu stehen in der Hilfe der App.
*Genauso wie in der App kann nicht mehr als ein Task zur gleichen Zeit laufen!*

### Starte Erfassung

Erfassung wird mit den eingestellten Einstellungen gestartet. Action:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Stoppe Erfassung

Erfassung wird sofort gestoppt. Action:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Starte Upload

Der Upload wird mit den in den Einstellungen gewählten Werten begonnen. Action:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Upload stoppen

Der Upload wird in Kürze abgebrochen. Action:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Export starten

Der Export wird mit der Konfiguration des letzten Laufs gestartet, die Messungen bleiben erhalten. Handlung:

`info.zamojski.soft.towercollector.EXPORT_START`

### Export stoppen

Der Export wird in Kürze abgebrochen. Handlung:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Auf Android Oreo und neuer müssen Sie zusätzlich den Namen des Komponentenpakets definieren:

`info.zamojski.soft.towercollector`