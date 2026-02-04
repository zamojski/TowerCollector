[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Tower Collector udostępnia zadania do uruchomienia poprzez wysyłanie *intencji* z innych aplikacji jak Tasker, Automate czy Llama. Musisz skorzystać z trybu *broadcast* dla wszystkich typów aktywności wymienionych poniżej. Więcej informacji o konfiguracji integracji znajdziesz w pomocy każdej z aplikacji.
*Tak samo jak w aplikacji, nie możesz uruchomić więcej niż jednego zadania w tym samym czasie!*

### Rozpocznij zbieranie

Zbieranie zostanie uruchomione z konfiguracją zdefiniowaną w Ustawieniach. Akcja:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Zatrzymaj zbieranie

Zbieranie zostanie natychmiastowo przerwane. Akcja:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Rozpocznij przesyłanie

Przesyłanie zostanie uruchomione z użyciem konfiguracji zdefiniowanej w Ustawieniach. Akcja:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Zatrzymaj przesyłanie

Przesyłanie zostanie wkrótce zatrzymane. Akcja:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Rozpocznij eksport

Eksport rozpocznie się z użyciem konfiguracji z poprzedniego uruchomienia, próbki nie zostaną skasowane. Akcja:

`info.zamojski.soft.towercollector.EXPORT_START`

### Zatrzymaj eksport

Eksport zostanie wkrótce zatrzymany. Akcja:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Na Androidzie Oreo i nowszych konieczne jest dodatkowo zdefiniowane nazwy pakietu komponentu:

`info.zamojski.soft.towercollector`