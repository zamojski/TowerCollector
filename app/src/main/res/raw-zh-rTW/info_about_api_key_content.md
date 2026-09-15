<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
存取權杖（API 金鑰）是一個特殊字串，用於授權使用者將擷取到的測量資料上傳至 OpenCellID 資料庫。

正確的密鑰必須由數字 0-9 和字母 a-f 組成。 根據註冊日期，密鑰格式可能如下所示：

* `pk.9743a66f914cc249efca164485a19c5c` - 32 個字符，前面有 `pk.`，共 35 個字符
* `9743a66f914cc249efca164485a19c5c` - 32 個字符
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 個字符，分為五組，以短橫線分隔，共 36 個字符
* `9743a66f914cc2` - 14 個字符
