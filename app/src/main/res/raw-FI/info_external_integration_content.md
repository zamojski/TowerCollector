<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector mahdollistaa tehtävien suorittamisen muilla sovelluksilla, jotka lähettävät *aikomuksia*, kuten Tasker, Automate tai Llama. Sinun on käytettävä *Broadcast* -tyyppiä kaikissa alla määritellyissä toiminnoissa. Lisätietoja tällaisen integroinnin tekemisestä saat kyseisen sovelluksen ohjeesta.
*Kuten sovelluksen sisällä, et voi käynnistää useampaa kuin yhtä tehtävää samanaikaisesti!*

### Käynnistä Collector

Collector käynnistetään Asetuksissa määritettyjen asetusten avulla. Toiminta:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### Pysäytä Collector

Collector pysäytetään välittömästi. Toiminta:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### Aloita lähetys

Lähetys aloitetaan Asetuksissa asetetuilla määrityksillä. Toiminta:

`info.zamojski.soft.towercollector.UPLOADER_START`

### Pysäytä lähetys

Lähetys perutaan pian. Toiminta:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### Aloita vienti

Vienti aloitetaan käyttämällä edellisen ajon asetuksia, mittaukset säilytetään. Toiminta:

`info.zamojski.soft.towercollector.EXPORT_START`

### Pysäytä vienti

Vienti perutaan pian. Toiminta:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### Android 8+

Android Oreossa ja uudemmissa versioissa sinun on lisäksi määritettävä komponenttipaketin nimi:

`info.zamojski.soft.towercollector`