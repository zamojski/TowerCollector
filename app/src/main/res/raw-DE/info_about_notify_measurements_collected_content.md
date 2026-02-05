<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector kann andere installierte Apps durch das Senden von *Intents* benachrichtigen. Mehr Informationen zum Empfangen und Konsumieren solcher Benachrichtigungen finden Sie in der Hilfe der App.

### Messungen-gesammelt Ereignis

Tower Collector kann jedes Mal einen Intent senden, wenn ein neues Satz an Messungen gesammelt wurde. Das kann in den Einstellungen aktiviert werden. Der Inhalt der Benachrichtigung ist eine JSON-Struktur mit Daten wie Uhrzeit und GPS-Position. Die Bedeutung von Feldnamen und Werten ist gleich wie in exportieren CSV-Dateien.

Bei Mobilfunkzellen hängt die Liste der Felder vom Typ des Netzes (net_type) ab. Nicht unterstützte oder fehlende Werte werden auf `null` gesetzt.

- GSM - mcc, mnc, lac, cell_id, asu, dbm, ta, rssi, arfcn
- UMTS - mcc, mnc, lac, cell_id, short_cell_id, rnc, psc, asu, dbm, ec_no, arfcn
- CDMA - sid → mnc, nid → lac, bid → cell_id, asu, dbm, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr
- LTE - mcc, mnc, tac → lac, ci → cell_id, short_cell_id, rnc, pci → psc, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, arfcn
- 5G NR - mcc, mnc, tac → lac, nci → cell_id, pci → psc, asu, dbm, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, arfcn
- TD-SCDMA - mcc, mnc, lac, cid → cell_id, cpid → psc, asu, dbm, rscp, arfcn

Action:

`info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED`

Extras Schlüssel:

`measurements`

Extras Wert (Beispiel):

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