<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
אסימון הגישה (מפתח ה־API) הוא מחרוזת מיוחדת שמשמשת לאימות המשתמש לצורך העלאת המדידות שנאספו אל מסד הנתונים של OpenCellID.

המפתח הנכון חייב להיות מורכב מספרות מ־0 עד 9 ומהאותיות a עד f. בהתאם לתאריך ההרשמה מבנה המפתח אמור להיראות כך:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 תווים, עם הקידומת `pk.‎`, סך הכול 35 תווים
* `9743a66f914cc249efca164485a19c5c` - 32 תווים
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 תווים בחמש קבוצות המופרדים על ידי מקף, סך הכל 36 תווים
* `9743a66f914cc2` - 14 תווים