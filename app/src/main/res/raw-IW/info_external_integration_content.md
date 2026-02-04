[//]: # (This Source Code Form is subject to the terms of the Mozilla Public)
[//]: # (License, v. 2.0. If a copy of the MPL was not distributed with this)
[//]: # (file, You can obtain one at http://mozilla.org/MPL/2.0/.)
&rlm;Tower Collector חושף משימות שניתן להריץ על ידי יישומונים אחרים ששולחים *intents* כגון Tasker,‏ Automate או Llama. עליך להשתמש בסוג *Broadcast* (שידור) לכל הפעילויות שמוגדרות להלן. למידע נוסף על אופן הביצוע של שילוב שכזה כדאי לעיין בעזרה של היישומון.
*כמו בתוך היישומון אין אפשרות להפעיל יותר ממשימה אחת בו־זמנית!*

### הפעלת האיסוף

מנגנון האיסוף יתחיל עם ההגדרות שבהעדפות: פעולה:

`info.zamojski.soft.towercollector.COLLECTOR_START`

### עצירת האיסוף

מנגנון האיסוף ייעצר מיידית. פעולה:

`info.zamojski.soft.towercollector.COLLECTOR_STOP`

### התחלת העלאה

העלאה תתחיל עם התצורה מההעדפות. פעולה:

`info.zamojski.soft.towercollector.UPLOADER_START`

### עצירת ההעלאה

ההעלאה תבוטל בקרוב. פעולה:

`info.zamojski.soft.towercollector.UPLOADER_STOP`

### התחלת ייצוא

הייצוא יתחיל עם ההגדרות מההפעלה האחרונה, המדדים יישמרו. פעולה:

`info.zamojski.soft.towercollector.EXPORT_START`

### עצירת ייצוא

הייצוא יבוטל בהקדם. פעולה:

`info.zamojski.soft.towercollector.EXPORT_STOP`

### &rlm;Android 8+

ב־Android Oreo וגרסאות חדשות יותר עליך להגדיר בנוסף שם חבילת רכיב:

`info.zamojski.soft.towercollector`