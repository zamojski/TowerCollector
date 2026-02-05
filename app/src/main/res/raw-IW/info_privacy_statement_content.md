<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
הפרטיות שלך מאוד חשובה, לכן היישומון אוסף ומעביר רק את המידע הנחוץ כדי לספק את השירותים. היישומון לא אוסף, מאחסן או שולח מידע שיכול לזהות אותך , את המכשיר שלך או פרטים נוספים עליך באופן אישי.

**אם מדיניות הפרטיות הזאת לא מקובלת עליך, נא לא להתקין או להסיר את היישומון. הסרת היישומון לצמיתות מהמכשיר הנייר שוות ערך להפסקת השימוש ביישומון.**

### התכונות העיקריות

The main functionality of the application is to collect cell tower identifiers, signal strength and the device location (coordinates). This information is stored locally on your device and will never be transmitted without your explicit consent and action. The collected data can be exported to the file persisted on your device or shared to OpenCellID.org and BeaconDB projects. To ensure the privacy is protected, the application does NOT use any proxy server that could access your data in transit. All the traffic to external services is encrypted using HTTPS protocol.

**Contribution to the OpenCellID.org** project requires a personal Access Token (API Key) that needs to be entered in the application preferences. The key will only be used to upload the measurements to the project database and will never leave the device in any other scenario. Anybody who knows the Access Token has full control over the uploaded data, including browsing and deleting. It\'s your responsibility to protect the confidentiality of your Access Token!

- [מדיניות הפרטיות של OpenCellID.org](https://community.opencellid.org/privacy)
- [The BeaconDB privacy notice](https://beacondb.net/privacy/)

ההרשאה למיקום הרקע נדרשת מכיוון שבתרחיש זה רוב תכונות היישומון דורשות את **הרשאת מיקום בחזית**, עם זאת, כדי להתחיל לאסוף מדידות עם הפעלת המכשיר, יש להעניק **הרשאת מיקום ברקע**. האיסוף מתבצע על ידי המערכת (ברקע) ולא על ידיך (בחזית).

### התכונות כרשות

תכונת ה **מפה** משתמשת בשירותים שמסופקים על ידי OpenStreetMap בכפוף ל[תנאי השימוש](https://wiki.osmfoundation.org/wiki/Terms_of_Use).

תכונת **בדיקת העדכונים האוטומטית** מתחברת לשרת כדי לראות אם יש גרסה עדכנית יותר של היישומון. הבקשה מכילה את גרסת היישומון המותקן והבדיקה מתבצעת עם הפעלת היישומון עד פעם ביום.

התכונה **יצירת קשר עם המפתחים** תיצור הודעת דוא״ל חדשה בלקוח הדוא״ל הנבחר. ההודעה כוללת את הפרטים הטכניים הבאים על המכשיר כדי לסייע בפתרון תקלות:

- גרסת היישומון
- גרסת ה־Android
- יצרן ודגם המכשיר

התכונה **העדפות ייצוא** או **ייצוא מסד נתונים** תעתיק את העדפות היישום או את מסד הנתונים לתיקייה היעודית במכשיר שלך. אם הקבצים ממוקמים בזיכרון המשותף, שלכל יישומון אחר יש גישה אליו, ייתכן שהנתונים הרגישים (כמו אסימון הגישה של OpenCellID) אינם מוגנים.

התכונה **רישום קבצים ברמת איתור תקלות** תיצור קובצי יומן רישום מפורטים מאוד בתיקייה הייעודית במכשיר שלך. היומן עשוי להכיל את המידע הרגיש, כמו המיקום הנוכחי שלך והאנטנה הסלולרית אליה מחובר המכשיר. יש להשתמש באפשרות זו באופן זמני רק לפתרון בעיות.

### מסייעי פתרון תקלות

היישומון אוסף את **סטטיסטיקות השימוש האלמוניות**. הנתונים משותפים רק עם המפתח, במטרה לעזור להבין טוב יותר כיצד נעשה שימוש ביישומון, מה זמני הביצוע של יכולות שונות ומהן התכונות הנפוצות ביותר. הנתונים שנאספים משמשים לשיפור הביצועים הכוללים, השימושיות וחוויית המשתמש. הנתונים נאספים באמצעות ספריית Google Analytics עבור Firebase מגורם צד־שלישי המסופקת על ידי Google תחת [מדיניות הפרטיות של Google](https://policies.google.com/privacy).

היישומון כולל מערכת ל**דיווח אוטומטי על קריסות**. דוחות הכשל ביישומונים משותפים רק עם המפתח ומכילים מידע על גורם הקריסה ועל תצורת המכשיר. הוא אינו כולל מידע שאפשר להשתמש בו כדי לזהות אותך או את המכשיר.