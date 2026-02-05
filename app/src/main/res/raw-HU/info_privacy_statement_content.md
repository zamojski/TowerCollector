<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
A magánélet védelme nagyon fontos, ezért az alkalmazás csak a szolgáltatások nyújtásához szükséges információkat gyűjti és továbbítja. Az alkalmazás NEM gyűjt, nem tárol vagy küld semmilyen információt, amely személyesen azonosíthatna téged, az eszközödet vagy bármilyen más személyes információt.

**Ha nem értesz egyet az adatvédelmi irányelvekkel, kérjük, ne telepítsd vagy távolítsd el az alkalmazást. Az alkalmazás végleges eltávolítása a mobileszközről egyenértékű az alkalmazás használatának megszüntetésével.**

### A főbb funkciók

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [Az OpenCellID.org adatvédelmi irányelvei](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

Az alkalmazás legtöbb funkciójának megköveteli az **előtér helymeghatározási engedélyét**, azonban a mérések gyűjtésének megkezdéséhez az eszköz indításakor meg kell adni a **háttér helymeghatározási engedélyt**. A háttér helymeghatározási engedélyre azért van szükség, mert ebben a forgatókönyvben a gyűjtést a rendszer (a háttérben) kezdeményezi, nem te (az előtérben).

### Az opcionális funkciók

A **térkép** funkció az OpenStreetMap által nyújtott szolgáltatásokat használja a [használati feltételek](https://wiki.osmfoundation.org/wiki/Terms_of_Use) alapján.

Az **automatikus frissítésellenőrzés** funkció csatlakozik a szerverhez, ahol ellenőrzi, van-e az alkalmazásnak elérhető újabb verziója. A kérelem tartalmazza a telepített alkalmazás verzióját, és naponta egyszer fut le az alkalmazás indításakor.

A **Fejlesztővel való kapcsolatfelvétel** funkció új e-mailt hoz létre a kiválasztott e-mail kliensben. Az üzenet a következő műszaki információkat tartalmazza az eszközről a hibaelhárítás elősegítése érdekében:

- Az alkalmazás verziója
- Az Android verzió
- A készülék gyártója és modellje

Az **Exportpreferenciák** vagy az **Adatbázis exportálása** funkció az alkalmazás beállításait vagy adatbázisát átmásolja az eszköz dedikált mappájába. Ha a fájlokat olyan megosztott memóriában helyezik el, amelyhez bármely más alkalmazás hozzáfér, akkor a bizalmas adatok (például az OpenCellID hozzáférési token) nem lesznek védettek.

A **fájlnaplózás hibakeresési szinten** funkció nagyon részletes naplófájlokat hoz létre az eszköz dedikált mappájában. A napló tartalmazhat bizalmas információkat, például az aktuális tartózkodási helyet és a cellatornyot, amelyhez az eszköz csatlakozik. Ezt az opciót csak ideiglenesen szabad használni a hibaelhárításhoz.

### A hibaelhárítás segítői

Az alkalmazás összegyűjti **az anonim használati statisztikákat**. Az adatokat csak a fejlesztővel osztják meg annak érdekében, hogy jobban megértsék az alkalmazás használatát, a különböző funkciók végrehajtási idejét és a legnépszerűbb szolgáltatásokat. Az összegyűjtött adatokat az általános teljesítmény, a használhatóság és a felhasználói élmény javítására használják. Az adatokat a Google által a Google által biztosított [Google adatvédelmi irányelvei](https://policies.google.com/privacy) alapján harmadik féltől származó Google Analytics for Firebase könyvtár segítségével gyűjtik.

Az alkalmazás beépítve tartalmazza az **automatizált összeomlási jelentés** rendszert. Az alkalmazáshiba-jelentéseket csak a fejlesztővel osztják meg, és információkat tartalmaznak az összeomlás okáról és az eszköz konfigurációjáról. NEM tartalmaz olyan információt, amely felhasználható az eszköz azonosítására.