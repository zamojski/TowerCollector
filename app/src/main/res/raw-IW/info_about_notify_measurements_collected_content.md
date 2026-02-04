[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
ל־Tower Collector יש אפשרות להודיע ליישומונים אחרים שמותקנים במערכת על ידי שידור *intents*. למידע נוסף על האזנה אליהם וצריכתם כמו התראות יש לעיין בעזרה של היישומון.

### אירוע מדידות שנאספו

מנגנון האיסוף יכול לשלוח intent בכל פעם שסדרה חדשה של בדיקות נאספת אם כך הוגדר בהגדרות. התוכן של ההתראות יהיה במבנה JSON עם דברים משותפים כמו זמן ו־GPS שיחולצו. שמות השדות והערכים ממופים באותו האופן כמו בקובץ CSV.

רשימת השדות בשימוש האנטנות תלויה בסוג (net_type). ערכים בלתי נתמכים או חסרים יוגדרו לכדי `null`.

- GSM - mcc, mnc, lac, cell_id, asu, dbm, ta, rssi, arfcn
- UMTS - mcc, mnc, lac, cell_id, short_cell_id, rnc, psc, asu, dbm, ec_no, arfcn
- CDMA - sid → mnc, nid → lac, bid → cell_id, asu, dbm, cdma_dbm, cdma_ecio, evdo_dbm, evdo_ecio, evdo_snr
- LTE - mcc, mnc, tac → lac, ci → cell_id, short_cell_id, rnc, pci → psc, ta, asu, dbm, rsrp, rsrq, rssi, rssnr, cqi, arfcn
- 5G NR - mcc, mnc, tac → lac, nci → cell_id, pci → psc, asu, dbm, csi_rsrp, csi_rsrq, csi_sinr, ss_rsrp, ss_rsrq, ss_sinr, arfcn
- TD-SCDMA - mcc, mnc, lac, cid → cell_id, cpid → psc, asu, dbm, rscp, arfcn

פעולה:

`info.zamojski.soft.towercollector.MEASUREMENTS_COLLECTED`

מפתח תוספות:

`measurements`

דוגמה לערך תוספות:

```
&lrm;{
    "measured_at" : 1234567890123,
    "gps" : {
        "lat" : 12.3213123,
        "lon" : -54.4535435543,
        "accuracy" : 12.0,
        "speed" : 13.21,
        "bearing" : 212.2,
        "altitude" : 121.0
    &lrm;},
    "cells" : [
    &lrm;{
            "mcc" : 260,
            "mnc" : 06,
            "lac" : 5115,
            "cell_id" : 214325525,
            "psc" : 1234,
            "asu" : 12,
            "dbm" : -112,
            "neighboring" : "false",
            "net_type" : "UMTS"
    &lrm;},
    &lrm;{
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
    &lrm;}
    &lrm;]
&lrm;}
```