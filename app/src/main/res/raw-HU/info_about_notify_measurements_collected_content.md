[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
A Tower Collector *szándékok* segítségével értesítheti a rendszeren telepített egyéb alkalmazásokat. Ha többet szeretnél tudni az ilyen értesítések figyeléséről és felhasználásáról, nézd meg az alkalmazás súgóját.

### Mérés begyűjtési esemény

Az adatgyűjtő képes egy szándékot küldeni minden alkalommal, ha egy új mérés került begyűjtésre, ha ez engedélyezve van a Beállításokban. Az értesítés tartalma egy JSON struktúra lesz a szokásos dolgokkal, mint az idő és a kinyert GPS pozíció. A mezőnevek és értékek ugyanúgy vannak megfeleltetve, mint az exportált CSV fájlban.

A cellák által használt mezők listája a típustól függ (net_type). A nem támogatott vagy hiányzó értékek `null` értéket kapnak.

- GSM - mcc, mnc, lac, cell_id, asu, dbm, ta, rssi, arfcn
- UMTS - mcc, mnc, lac, cell_id, short_cell_id, rnc, psc, asu, dbm, ec_no, arfcn
- CDMA - sid → mnc, nid → lac, bid → cell_id, asu, dbm, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr
- LTE - mcc, mnc, tac → lac, ci → cell_id, short_cell_id, rnc, pci → psc, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, arfcn
- 5G NR - mcc, mnc, tac → lac, nci → cell_id, pci → psc, asu, dbm, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, arfcn
- TD-SCDMA - mcc, mnc, lac, cid → cell_id, cpid → psc, asu, dbm, rscp, arfcn

Művelet:

`info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED`

Az extrák kulcsa:

`measurements`

Az extra értékek példája:

```
{
    "measured_at" : 1234567890123,
    "gps" : {
        "lat" : 12.3213123,
        "lon" : -54.4535435543,
        "accuracy" : 12.0,
        "speed" : 13.21,
        "bearing" : 212.2,
        "altitude" : 121.0
    },
    "cells" : [
        {
            "mcc" : 260,
            "mnc" : 06,
            "lac" : 5115,
            "cell_id" : 214325525,
            "psc" : 1234,
            "asu" : 12,
            "dbm" : -112,
            "neighboring" : "false",
            "net_type" : "UMTS"
        },
        {
            "mcc" : null,
            "mnc" : 16,
            "lac" : 5115,
            "cell_id" : 2143255,
            "psc" : null,
            "asu" : 13,
            "dbm" : -112,
            "ta" : null,
            "neighboring" : "false",
            "net_type" : "CDMA",
            "cdma_dbm" : null,
            "cdma_ecio" : null,
            "evdo_dbm" : null,
            "evdo_ecio" : null,
            "evdo_snr" : null
        }
    ]
}
```