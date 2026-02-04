[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Sie können angeben, welche Methode verwendet werden soll um die Infos der Basisstationen zu sammeln.

*Es wird empfohlen, die neueste zu verwenden um verlässlichere Daten zu erhalten. Diese Auswahl ist nicht verfügbar auf Geräten mit Android 4.1 und älter.*

* **Automatisch** - Tower Collector wird automatisch erkennen, welche Version der API verwendet werden kann und die beste auswählen. Es wird die sicherste Option auswählen wenn die Erfassung ohne Empfang gestartet wird.
* **Android 4.2 API** - Verwendet Methoden, die auf Android 4.2 und neuer verfügbar sind. Diese Methoden bieten die verlässlichsten Zelleninfos. Allerdings haben manche Hersteller das neue API nicht korrekt implementiert und so kann es auf solchen Geräten nicht verwendet werden. Unter Android 5.1 und neuer können zusätzlich auch Basisstationen von mehreren SIM-Karten erfasst werden.
* **Android 1.0 API** - Verwendet eine Reihe von Methoden, die seit der ersten Version von Android verfügbar sind. Diese Methoden können allerdings unzuverlässige Daten bei LTE- und UMTS-Netzen zurück liefern. Abhängig vom Gerät kann es erforderlich sein, den Bildschirm während der Erfassung eingeschaltet zu lassen damit die Zelleninfos korrekt aktualisiert werden.