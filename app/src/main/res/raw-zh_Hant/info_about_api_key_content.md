<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
存取權杖（Access Token／API Key） 是一組特殊字串，用於驗證使用者身分，允許其將所收集的量測資料上傳至 OpenCellID 資料庫

正確的金鑰必須由 0–9 的數字以及 a–f 的字母所組成。\
依註冊日期不同，金鑰格式可能會如下所示：

* `pk.9743a66f914cc249efca164485a19c5c` - 32 characters, preceded with `pk.`,
  total 35 characters
* `9743a66f914cc249efca164485a19c5c` - 32 characters
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 characters in five groups
  separated by dash, total 36 characters
* `9743a66f914cc2` - 14 characters
