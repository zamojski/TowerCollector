[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Yksityisyys on erittäin tärkeää, joten sovellus kerää ja välittää vain tarvittavat tiedot palvelujen tarjoamiseksi. Sovellus EI kerää, tallenna tai lähetä mitään tietoja, joista voisi tunnistaa sinut, laitteesi tai muita henkilökohtaisia tietoja.

**Jos et hyväksy tätä tietosuojakäytäntöä, älä asenna tai poista sovellus. Sovelluksen pysyvä poistaminen mobiililaitteesta vastaa sovelluksen käytön lopettamista.**

### Päätoiminnot

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [OpenCellID.orgin tietosuojakäytäntö](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Useimmat sovelluksen ominaisuudet edellyttävät **etualalla sijaintilupaa**, mutta mittausten keräämisen aloittamiseksi laitteen käynnistyksen yhteydessä on myönnettävä **taustasijaintilupa**. Taustapaikannusoikeus tarvitaan, koska tässä skenaariossa keräyksen aloittaa järjestelmä (taustalla), et sinä (etualalla).

### Valinnaiset toiminnot

**Karttaominaisuus** käyttää OpenStreetMapin tarjoamia palveluita [käyttöehtojen](https://wiki.osmfoundation.org/wiki/Terms_of_Use) mukaisesti.

**Automaattinen päivityksen tarkistus** -ominaisuus muodostaa yhteyden palvelimeen ja tarkistaa, onko sovelluksesta saatavilla uudempi versio. Pyyntö sisältää asennetun sovelluksen version, ja se suoritetaan sovelluksen käynnistyksen yhteydessä kerran päivässä.

**Ota yhteyttä kehittäjään** -ominaisuus luo uuden sähköpostiviestin valitussa sähköpostiohjelmassa. Viesti sisältää seuraavat laitteen tekniset tiedot, jotka auttavat vianmäärityksessä:

- Sovelluksen versio
- Android-versio
- Laitteen valmistaja ja malli

**Vie asetukset** tai **Vie tietokanta** -toiminto kopioi sovelluksen asetukset tai tietokannan laitteesi omaan kansioon. Jos tiedostot sijoitetaan jaettuun muistiin, johon muilla sovelluksilla on pääsy, arkaluonteiset tiedot (kuten OpenCellID Access Token) voivat olla suojaamattomia.

**Tiedostojen kirjaaminen** Debug-tasolla -toiminto luo erittäin yksityiskohtaisia lokitiedostoja laitteesi omaan kansioon. Loki voi sisältää arkaluonteisia tietoja, kuten nykyisen sijaintisi ja matkapuhelinmaston, johon laitteesi on yhdistetty. Tätä vaihtoehtoa tulisi käyttää vain tilapäisesti vianmääritykseen.

### Vianetsintäavustajat

Sovellus kerää **nimettömiä käyttötilastoja**. Tiedot jaetaan vain kehittäjän kanssa, jotta voidaan ymmärtää paremmin, miten sovellusta käytetään, mitkä ovat eri toimintojen suoritusajat ja mitkä ovat suosituimmat ominaisuudet. Kerättyjä tietoja käytetään yleisen suorituskyvyn, käytettävyyden ja käyttäjäkokemuksen parantamiseen. Tiedot kerätään käyttämällä kolmannen osapuolen Google Analytics for Firebase -kirjastoa, jonka Google tarjoaa [Googlen tietosuojakäytännön](https://policies.google.com/privacy) mukaisesti.

Sovelluksessa on sisäänrakennettu **automaattinen kaatumisraportointijärjestelmä**. Sovelluksen virheraportit jaetaan vain kehittäjän kanssa ja ne sisältävät tietoja kaatumisen syystä ja laitteen määrityksistä. Se EI sisällä tietoja, joita voidaan käyttää sinun tai laitteen tunnistamiseen.