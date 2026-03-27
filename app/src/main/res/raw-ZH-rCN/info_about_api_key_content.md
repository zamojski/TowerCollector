<!-- This Source Code Form is subject to the terms of the Mozilla Public -->
<!-- License, v. 2.0. If a copy of the MPL was not distributed with this -->
<!-- file, You can obtain one at http://mozilla.org/MPL/2.0/. -->
访问令牌（API 密钥）是用于授权用户将收集到的测量数据上传到 OpenCellID 数据库的特殊字符串。

正确的密钥必须由数字 0-9 和字母 a-f 组成。根据注册日期，密钥格式可能如下所示：

* `pk.9743a66f914cc249efca164485a19c5c` - 32 个字符，前面带有 `pk.`，共 35 个字符
* `9743a66f914cc249efca164485a19c5c` - 32 个字符
* `9743a66f-914c-c249-efca-164485a19c5c` - 32 个字符，分为五组，以短横线分隔，共 36 个字符
* `9743a66f914cc2` - 14 个字符
