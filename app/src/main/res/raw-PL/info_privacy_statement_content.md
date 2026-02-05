<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Prywatność jest bardzo ważna, dlatego aplikacja zbiera i przesyła wyłącznie informacje konieczne do świadczenia funkcjonowania. Aplikacja NIE zbiera, NIE przechowuje, ani NIE przesyła żadnych informacji, które mogłyby zostać wykorzystane do zidentyfikowania ciebie, twojego urządzenia, ani żadnej informacji osobistej.

**Jeżeli nie zgadzasz się z tą polityką prywatności, nie instaluj lub odinstaluj aplikację. Trwałe usunięcie aplikacji z urządzenia mobilnego jest równoważne zaprzestaniu korzystania z aplikacji.**

### Główne funkcje

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [Polityka prywatności OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Większość funkcji aplikacji wymaga **dostępu do lokalizacji urządzenia podczas działania**, jednakże, aby automatycznie uruchomić zbieranie próbek przy uruchamianiu urządzenia, konieczne jest przyznanie **dostępu do lokalizacji urządzenia w tle**. Dostęp do lokalizacji w tle jest wymagany, ponieważ w tym przypadku zbieranie jest uruchamiane przez system (w tle), a nie przez ciebie (na pierwszym planie).

### Funkcje opcjonalne

**Mapa** korzysta z usług dostarczanych przez OpenStreetMap na określonych [warunkach użytkowania](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

**Automatyczne sprawdzanie aktualizacji** łączy się do serwera w celu sprawdzenia, czy jest dostępna nowsza wersja aplikacji. Żądanie zawiera numer wersji zainstalowanej aplikacji i jest wykonywane przy starcie aplikacji raz dziennie.

**Kontakt z twórcą** tworzy nową wiadomość email w wybranej aplikacji pocztowej. Wiadomość zawiera podstawowe dane techniczne urządzenia, które mogą być przydatne przy rozwiązywaniu problemów:

- Wersja aplikacji
- Wersja Androida
- Producent i model urządzenia

**Eksport ustawień** i **Eksport bazy danych** wykonują kopię ustawień lub bazy danych aplikacji do wybranego folderu w pamięci urządzenia. Jeżeli wybrany folder znajduje się w pamięci współdzielonej, do której dostęp mają inne aplikacje, wówczas wrażliwe dane (takie jak klucz dostępu OpenCellID) mogą nie być chronione.

**Logowanie do pliku na poziomie diagnostycznym** tworzy bardzo szczegółowe pliki logów w wybranym folderze w pamięci urządzenia. Log może zawierać wrażliwe dane, takie jak twoje obecne położenie i nadajnik do którego urządzenia jest podłączone. Ta opcja powinna być używana tymczasowo w celu rozwiązywania problemów.

### Pomoc w diagnostyce problemów

Aplikacja zbiera **anonimowe statystyki użycia**. Dane są udostępniane wyłącznie twórcy aplikacji w celu lepszego zrozumienia jak aplikacja jest wykorzystywana, jakie są czasy działania poszczególnych funkcji oraz jakie funkcje są używane najczęściej. Zebrane dane służą do poprawy ogólnej wydajności aplikacji, użyteczności i komfortu użytkowania. Dane zbierane są za pomocą zewnętrznej biblioteki Google Analytics for Firebase dostarczanej przez Google i przetwarzane zgodnie z [polityką prywatności Google](https://policies.google.com/privacy).

Aplikacja posiada wbudowany mechanizm **automatycznego raportowania błędów**. Raporty błędów aplikacji są dostępne wyłącznie dla twórcy aplikacji i zawierają informacje o przyczynie błędu i konfiguracji urządzenia. Raporty NIE zawierają informacji pozwalających na zidentyfikowanie ciebie, ani twojego urządzenia.