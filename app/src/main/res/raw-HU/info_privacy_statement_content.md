<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
A magánélet védelme nagyon fontos, ezért az alkalmazás csak a szolgáltatások
nyújtásához szükséges információkat gyűjti és továbbítja. Az alkalmazás NEM
gyűjt, nem tárol vagy küld semmilyen információt, amely személyesen
azonosíthatna téged, az eszközödet vagy bármilyen más személyes információt.

**Ha nem értesz egyet az adatvédelmi irányelvekkel, kérjük, ne telepítsd vagy
távolítsd el az alkalmazást. Az alkalmazás végleges eltávolítása a
mobileszközről egyenértékű az alkalmazás használatának megszüntetésével.**

### A főbb funkciók

Az alkalmazás fő funkciója a cellatornyok azonosítóinak, a jelerősségnek és az
eszköz helyének (koordinátáinak) gyűjtése. Ezeket az információkat a rendszer
lokálisan tárolja az eszközén, és az Ön kifejezett hozzájárulása és beavatkozása
nélkül soha nem továbbítja. A gyűjtött adatok exportálhatók az eszközén tárolt
fájlba, vagy megoszthatók az OpenCellID.org és a BeaconDB projektekkel. Az
adatvédelem biztosítása érdekében az alkalmazás NEM használ olyan
proxykiszolgálót, amely hozzáférhetne az átvitel alatt álló adataihoz. A külső
szolgáltatások felé irányuló összes forgalom HTTPS protokollal titkosított.

**Az OpenCellID.org projekthez való hozzájáruláshoz** személyes hozzáférési
token (API-kulcs) szükséges, amelyet az alkalmazás beállításaiban kell megadni.
A kulcsot csak a mérések projektadatbázisba való feltöltésére használjuk, és
semmilyen más esetben sem hagyja el az eszközt. Bárki, aki ismeri a hozzáférési
tokent, teljes hozzáféréssel rendelkezik a feltöltött adatok felett, beleértve a
böngészést és a törlést is. Az Ön felelőssége a hozzáférési token
bizalmasságának védelme!

- [Az OpenCellID.org adatvédelmi
  irányelvei](https://community.opencellid.org/privacy)
- [A BeaconDB adatvédelmi nyilatkozata](https://beacondb.net/privacy/)

Az alkalmazás legtöbb funkciójának megköveteli az **előtér helymeghatározási
engedélyét**, azonban a mérések gyűjtésének megkezdéséhez az eszköz indításakor
meg kell adni a **háttér helymeghatározási engedélyt**. A háttér
helymeghatározási engedélyre azért van szükség, mert ebben a forgatókönyvben a
gyűjtést a rendszer (a háttérben) kezdeményezi, nem te (az előtérben).

### Az opcionális funkciók

A **térkép** funkció az OpenStreetMap által nyújtott szolgáltatásokat használja
a [használati feltételek](https://wiki.osmfoundation.org/wiki/Terms_of_Use)
alapján.

Az **automatikus frissítésellenőrzés** funkció csatlakozik a szerverhez, ahol
ellenőrzi, van-e az alkalmazásnak elérhető újabb verziója. A kérelem tartalmazza
a telepített alkalmazás verzióját, és naponta egyszer fut le az alkalmazás
indításakor.

A **Fejlesztővel való kapcsolatfelvétel** funkció új e-mailt hoz létre a
kiválasztott e-mail kliensben. Az üzenet a következő műszaki információkat
tartalmazza az eszközről a hibaelhárítás elősegítése érdekében:

- Az alkalmazás verziója
- Az Android verzió
- A készülék gyártója és modellje

Az **Exportpreferenciák** vagy az **Adatbázis exportálása** funkció az
alkalmazás beállításait vagy adatbázisát átmásolja az eszköz dedikált mappájába.
Ha a fájlokat olyan megosztott memóriában helyezik el, amelyhez bármely más
alkalmazás hozzáfér, akkor a bizalmas adatok (például az OpenCellID hozzáférési
token) nem lesznek védettek.

A **fájlnaplózás hibakeresési szinten** funkció nagyon részletes naplófájlokat
hoz létre az eszköz dedikált mappájában. A napló tartalmazhat bizalmas
információkat, például az aktuális tartózkodási helyet és a cellatornyot,
amelyhez az eszköz csatlakozik. Ezt az opciót csak ideiglenesen szabad használni
a hibaelhárításhoz.

### A hibaelhárítás segítői

Az alkalmazás összegyűjti **az anonim használati statisztikákat**. Az adatokat
csak a fejlesztővel osztják meg annak érdekében, hogy jobban megértsék az
alkalmazás használatát, a különböző funkciók végrehajtási idejét és a
legnépszerűbb szolgáltatásokat. Az összegyűjtött adatokat az általános
teljesítmény, a használhatóság és a felhasználói élmény javítására használják.
Az adatokat a Google által a Google által biztosított [Google adatvédelmi
irányelvei](https://policies.google.com/privacy) alapján harmadik féltől
származó Google Analytics for Firebase könyvtár segítségével gyűjtik.

Az alkalmazás beépítve tartalmazza az **automatizált összeomlási jelentés**
rendszert. Az alkalmazáshiba-jelentéseket csak a fejlesztővel osztják meg, és
információkat tartalmaznak az összeomlás okáról és az eszköz konfigurációjáról.
NEM tartalmaz olyan információt, amely felhasználható az eszköz azonosítására.
