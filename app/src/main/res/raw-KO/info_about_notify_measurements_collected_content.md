<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
Tower Collector은 *intents*를 브로드캐스트하여 장치에 설치된 다른 앱에 전송할 수 있습니다. 이러한 알림을 수신하고 사용하는 방법에 대한 자세한 내용은 해당 앱의 도움말을 확인하세요.

### 데이터 수집 이벤트

설정된 경우 새로운 데이터가 측정 될 때마다 intent를 브로드캐스트합니다. 알림의 내용은 시간 및 GPS와 같은 JSON 구조의 데이터입니다. 필드 이름과 값은 CSV를 내보낼 때 내용과 동일한 방식으로 맵핑됩니다.

기지국에서 사용하는 필드 목록은 유형(net_type)에 따라 다릅니다. 지원되지 않거나 누락된 값은 `null`로 설정됩니다.

- GSM - mcc, mnc, lac, cell_id, asu, dbm, ta, rssi, arfcn
- UMTS - mcc, mnc, lac, cell_id, short_cell_id, rnc, psc, asu, dbm, ec_no, arfcn
- CDMA - sid → mnc, nid → lac, bid → cell_id, asu, dbm, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr
- LTE - mcc, mnc, tac → lac, ci → cell_id, short_cell_id, rnc, pci → psc, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, arfcn
- 5G NR - mcc, mnc, tac → lac, nci → cell_id, pci → psc, asu, dbm, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, arfcn
- TD-SCDMA - mcc, mnc, lac, cid → cell_id, cpid → psc, asu, dbm, rscp, arfcn

Action:

`info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED`

Extras key:

`measurements`

Extras value 예제:

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