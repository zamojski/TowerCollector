[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Możesz wybrać, jaka metoda będzie używana do odczytywania informacji o przekaźniku komórkowym w trakcie zbierania.

*Wysoce zalecane jest skorzystanie z najnowszej dostępnej metody, aby uzyskać bardziej wiarygodne dane. Wybór jest niedostępny na urządzeniach z Androidem 4.1 lub starszym.*

*   **Wykryj automatycznie** - Aplikacja automatycznie wykryje, której wersji API może użyć i z niej skorzysta. Jeżeli telefon nie będzie miał zasięgu wybrana zostanie bezpieczna opcja.
* **Android API 4.2** - Używa metod dostępnych na Androidzie 4.2 i nowszych. Metody te zapewniają najbardziej wiarygodne dane przekaźnika. Jednakże niektórzy producenci nie wdrożyli nowego API prawidłowo i na niektórych urządzeniach może ono być niedostępne. Na Androidzie 5.1 i nowszych zbierane będą przekaźniki z wielu kart SIM.
* **Android 1.0 API** - Używa zestawu metod dostępnych od pierwszej wersji Androida. Metody te mogą zwracać nieprawidłowe dane w przypadku sieci LTE i UMTS. W zależności od urządzenia konieczne może być utrzymywanie ekranu włączonego podczas zbierania, aby informacje o przekaźniku były aktualizowane prawidłowo.