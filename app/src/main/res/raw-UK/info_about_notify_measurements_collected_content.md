[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
Колектор сот може повідомити інші додатки, встановлені в системі, транслюючи *intents*. Щоб отримати додаткові відомості про те, як прослухати та використовувати такі сповіщення, перевірте в інструкції додатку.

### Виміри зібраних подій

Колектор може відправити дані кожен раз, коли збирається новий набір вимірювань, якщо встановлено в налаштуваннях. Зміст сповіщення буде структурою JSON із загальними даними, такими як час та координати GPS. Імена і значення полів відображаються так само, як у експортованому файлі CSV.

Список полів, що використовуються клітинками, залежить від типу (net_type). Непідтримувані чи відсутні значення будуть встановлені як ` null `.

- GSM - mcc, mnc, lac, cell_id, asu, dbm, ta, rssi, arfcn
- UMTS - mcc, mnc, lac, cell_id, short_cell_id, rnc, psc, asu, dbm, ec_no, arfcn
- CDMA - sid → mnc, nid → lac, bid → cell_id, asu, dbm, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr
- LTE - mcc, mnc, tac → lac, ci → cell_id, short_cell_id, rnc, pci → psc, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, arfcn
- 5G NR - mcc, mnc, tac → lac, nci → cell_id, pci → psc, asu, dbm, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, arfcn
- TD-SCDMA - mcc, mnc, lac, cid → cell_id, cpid → psc, asu, dbm, rscp, arfcn

Дія:

`info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED`

Додаткові ключі:

`measurements`

Приклад додаткових значень:

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