<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
אסימון גישה (מפתח API) הוא מחרוזת מיוחדת המשמשת לאשר למשתמש להעלות מדידות שנאספו
למסד הנתונים של OpenCellID.

המפתח הנכון חייב להכיל ספרות 0-9 ואותיות a-f. בהתאם לתאריך הרישום, פורמט המפתח
עשוי להיראות כך:

* `pk.9743a66f914cc249efca164485a19c5c` - 32 תווים, שלפניהם `pk.`, סה"כ 35 תווים
* `9743a66f914cc249efca164485a19c5c` - 32 תווים
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 תווים בחמש קבוצות המופרדות באמצעות
  מקף, סה"כ 36 תווים
* `9743a66f914cc2` - 14 תווים
