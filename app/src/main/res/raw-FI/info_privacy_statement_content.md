<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Yksityisyys on erittäin tärkeää, joten sovellus kerää ja välittää vain
tarvittavat tiedot palvelujen tarjoamiseksi. Sovellus EI kerää, tallenna tai
lähetä mitään tietoja, joista voisi tunnistaa sinut, laitteesi tai muita
henkilökohtaisia tietoja.

**Jos et hyväksy tätä tietosuojakäytäntöä, älä asenna tai poista sovellus.
Sovelluksen pysyvä poistaminen mobiililaitteesta vastaa sovelluksen käytön
lopettamista.**

### Päätoiminnot

Sovelluksen päätoiminto on kerätä tukiasemien tunnisteita, signaalin
voimakkuutta ja laitteen sijaintia (koordinaatteja). Nämä tiedot tallennetaan
paikallisesti laitteellesi, eikä niitä koskaan lähetetä ilman nimenomaista
suostumustasi ja toimiasi. Kerätyt tiedot voidaan viedä laitteellesi
tallennettuun tiedostoon tai jakaa OpenCellID.org- ja BeaconDB-projekteihin.
Yksityisyyden suojaamiseksi sovellus EI käytä välityspalvelinta, joka voisi
käyttää tietojasi siirron aikana. Kaikki ulkoisiin palveluihin suuntautuva
liikenne salataan HTTPS-protokollalla.

**OpenCellID.org-projektiin osallistuminen** vaatii henkilökohtaisen
käyttöoikeustunnuksen (API-avaimen), joka on syötettävä sovelluksen asetuksiin.
Avainta käytetään vain mittausten lataamiseen projektin tietokantaan, eikä se
koskaan poistu laitteesta missään muussa tilanteessa. Jokaisella, joka tietää
käyttöoikeustunnuksen, on täysi hallinta ladattuihin tietoihin, mukaan lukien
selaaminen ja poistaminen. On sinun vastuullasi suojata käyttöoikeustunnuksesi
luottamuksellisuus!

- [OpenCellID.orgin
  tietosuojakäytäntö](https://community.opencellid.org/privacy)
- [BeaconDB:n tietosuojailmoitus](https://beacondb.net/privacy/)

Useimmat sovelluksen ominaisuudet edellyttävät **etualalla sijaintilupaa**,
mutta mittausten keräämisen aloittamiseksi laitteen käynnistyksen yhteydessä on
myönnettävä **taustasijaintilupa**. Taustapaikannusoikeus tarvitaan, koska tässä
skenaariossa keräyksen aloittaa järjestelmä (taustalla), et sinä (etualalla).

### Valinnaiset toiminnot

**Karttaominaisuus** käyttää OpenStreetMapin tarjoamia palveluita
[käyttöehtojen](https://wiki.osmfoundation.org/wiki/Terms_of_Use) mukaisesti.

**Automaattinen päivityksen tarkistus** -ominaisuus muodostaa yhteyden
palvelimeen ja tarkistaa, onko sovelluksesta saatavilla uudempi versio. Pyyntö
sisältää asennetun sovelluksen version, ja se suoritetaan sovelluksen
käynnistyksen yhteydessä kerran päivässä.

**Ota yhteyttä kehittäjään** -ominaisuus luo uuden sähköpostiviestin valitussa
sähköpostiohjelmassa. Viesti sisältää seuraavat laitteen tekniset tiedot, jotka
auttavat vianmäärityksessä:

- Sovelluksen versio
- Android-versio
- Laitteen valmistaja ja malli

**Vie asetukset** tai **Vie tietokanta** -toiminto kopioi sovelluksen asetukset
tai tietokannan laitteesi omaan kansioon. Jos tiedostot sijoitetaan jaettuun
muistiin, johon muilla sovelluksilla on pääsy, arkaluonteiset tiedot (kuten
OpenCellID Access Token) voivat olla suojaamattomia.

**Tiedostojen kirjaaminen** Debug-tasolla -toiminto luo erittäin
yksityiskohtaisia lokitiedostoja laitteesi omaan kansioon. Loki voi sisältää
arkaluonteisia tietoja, kuten nykyisen sijaintisi ja matkapuhelinmaston, johon
laitteesi on yhdistetty. Tätä vaihtoehtoa tulisi käyttää vain tilapäisesti
vianmääritykseen.

### Vianetsintäavustajat

Sovellus kerää **nimettömiä käyttötilastoja**. Tiedot jaetaan vain kehittäjän
kanssa, jotta voidaan ymmärtää paremmin, miten sovellusta käytetään, mitkä ovat
eri toimintojen suoritusajat ja mitkä ovat suosituimmat ominaisuudet. Kerättyjä
tietoja käytetään yleisen suorituskyvyn, käytettävyyden ja käyttäjäkokemuksen
parantamiseen. Tiedot kerätään käyttämällä kolmannen osapuolen Google Analytics
for Firebase -kirjastoa, jonka Google tarjoaa [Googlen
tietosuojakäytännön](https://policies.google.com/privacy) mukaisesti.

Sovelluksessa on sisäänrakennettu **automaattinen
kaatumisraportointijärjestelmä**. Sovelluksen virheraportit jaetaan vain
kehittäjän kanssa ja ne sisältävät tietoja kaatumisen syystä ja laitteen
määrityksistä. Se EI sisällä tietoja, joita voidaan käyttää sinun tai laitteen
tunnistamiseen.
